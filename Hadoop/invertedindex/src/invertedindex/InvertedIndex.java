package invertedindex;

import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class InvertedIndex extends Configured implements Tool{
	public int run(String[] args) throws Exception {
		
		//Read file of stop words and extract each word into a string
		BufferedReader stopFile = new BufferedReader(new FileReader("stopwords_google.txt"));
		String stopWords = stopFile.lines().filter(x -> !x.startsWith("#"))
						.reduce(" ", (x, y) -> x+" "+y); //split the string with spaces
		
		Configuration conf = new Configuration();
		conf.set("textinputformat.record.delimiter", "</Document>"); //Separate input to separate documents 
		conf.set("stopWords", stopWords);//set stopWords to access in IndexMapper 
		
		Job job = Job.getInstance(conf, "word count");
		job.setJarByClass(InvertedIndex.class);
		
		job.setSortComparatorClass(CustomComparator.class);//Custom Comparator
		job.setPartitionerClass(CustomPartitioner.class);//Customer Partitioner

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(MyResultWritable.class);//Output is a custom writable class

		job.setMapperClass(IndexMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(MyResultWritable.class);//Mapper output is a custom writable class
		job.setReducerClass(IndexReducer.class);
		job.setNumReduceTasks(10);
		job.setCombinerClass(IndexReducer.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		boolean success = job.waitForCompletion(true);
		return success ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		final int result;
		if (args.length < 2) {
			System.out.println("Arguments: inputDirectory outputDirectory");
			result = -1;
		} else {
			result = ToolRunner.run(new InvertedIndex(), args);
		}
		System.exit(result);
	}
}

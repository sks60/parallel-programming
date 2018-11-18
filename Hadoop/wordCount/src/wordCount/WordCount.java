package wordCount;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.hadoop.util.*;

/**
 * Example WordCount Hadoop program.
 *
 * This will count the number of times each word appears in the input files.
 *
 * @author Hadoop distribution
 */
public class WordCount extends Configured implements Tool {

	public int run(String[] args) throws Exception {
	       
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf,"word count");
		job.setJarByClass(WordCount.class);
	 
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.setMapperClass(WordMapper.class);
		job.setReducerClass(WordReducer.class);
		job.setNumReduceTasks(2);
		
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
			result = ToolRunner.run(new WordCount(), args);
		}
		System.exit(result);
	}
}

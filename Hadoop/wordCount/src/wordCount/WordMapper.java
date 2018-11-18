package wordCount;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


	public class WordMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
		
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();

		/**
		 * Maps word and adds one to each
		 * 
		 * @param LongWritable key 
		 * @param Text value (all words in text)
		 * @param Context context
		 * 
		 */
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
			String line = value.toString();//change to String
			StringTokenizer tokenizer = new StringTokenizer(line); //to split line to words
			
			while (tokenizer.hasMoreTokens()) { //while there are more words continue
				word.set(tokenizer.nextToken()); //get word from line by token
				context.write(word, one);
			}
		}
	}


package wordCount;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class WordReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
	
	private IntWritable result = new IntWritable();

	/**
	 * Counts the number of occurrences of each word
	 * 
	 * @param Text key (word)
	 * @param Iterable<IntWritable> values (iterable of the (word, one) from mapper class)
	 * @param Context context
	 * 
	 */
	public void reduce(Text key, Iterable<IntWritable> values, Context context)
			throws IOException, InterruptedException {

		int sum = 0;
		//count the sum 
		for (IntWritable val : values) {
			sum += val.get();
		}
		result.set(sum);
		
		//limit to display words only if sum of occurrences is greater than 500
		if (sum >= 500) context.write(key, result);
	}
}

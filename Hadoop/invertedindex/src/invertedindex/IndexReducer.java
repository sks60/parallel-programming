package invertedindex;

import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class IndexReducer extends Reducer<Text, MyResultWritable, Text, MyResultWritable> {
	
	//private MyMapWritable result = new MyMapWritable();
	private MyResultWritable result = new MyResultWritable();
	
	/**
	 * Reducer to take MyResultWritable and map them 
	 * 
	 * @param key (word)
	 * @param MyResultWritable values as an Iterable 
	 * @param Context
	 */
	public void reduce(Text key, Iterable<MyResultWritable> values, Context context)
			throws IOException, InterruptedException {
		result.clear();
		for (MyResultWritable positions : values) {
			result.take(positions, context); //create an reduced mapped object
		}
		context.write(key, result); //write/output word and the result object made in MyresultWritable
		
		
	}
}
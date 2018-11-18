package invertedindex;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class CustomPartitioner extends Partitioner<Text, MyResultWritable> {

	final int MAX_WORD_LENGTH = 30;
	
	/**
	 * A Custom Partitioner method to divide the input to partitions
	 * (This is not the optimal distribution)
	 * 
	 * @param Text key
	 * @param MyResultWritable value
	 * @param int numPartitions
	 * 
	 * @return integer of which partition it should go to in
	 */
	@Override
	public int getPartition(Text key, MyResultWritable value, int numPartitions) {
		
		int length = key.getLength(); //word length
		int division  = MAX_WORD_LENGTH / numPartitions; //how many partitions are possible
		
		int partitionChoice = (length-1)/division; //which partition number is chosen
		
		//catch case where the partition choiceis greater than the number of partitions available
		if(partitionChoice >= numPartitions) {
			partitionChoice = numPartitions -1; // correct it by choosing the last partition 
		}
		
		return partitionChoice;
	}


}

package invertedindex;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

public class MyResultWritable implements Writable {
	public int totalCount = 0;

	//object to hold result
	public Map<Integer, ArrayList<Integer>> mappingResult = new HashMap<Integer, ArrayList<Integer>>();
	public Map<Integer, Integer> docCount = new HashMap<Integer, Integer>();

	/**
	 * Override method to write the results
	 * 
	 * @param DataOutput of what to write out
	 */
	@Override
	public void write(DataOutput out) throws IOException {

		out.writeInt(totalCount);// totalCount
		out.writeInt(docCount.size());// numDocs
		for (Integer docId : mappingResult.keySet()) {
			out.writeInt(docId);// docId
			out.writeInt(docCount.get(docId));// read_docCount
			for (Integer listEntry : mappingResult.get(docId)) {
				out.writeInt(listEntry);// listEntry
			}
		}
	}
	
	/**
	 * Override method to read the results that where written
	 * 
	 * @param DataInput of what to read in
	 */
	@Override
	public void readFields(DataInput in) throws IOException {

		clear();
		totalCount = in.readInt();// totalCount
		int numDocs = in.readInt();// numDocs
		for (int i = 0; i < numDocs; i++) {
			int docId = in.readInt();// docId
			int read_docCount = in.readInt();// read_docCount
			ArrayList<Integer> listEntry = new ArrayList<Integer>();
			for (int j = 0; j < read_docCount * 2; j++) {
				listEntry.add(in.readInt());// listEntry
			}
			mappingResult.put(docId, listEntry);
			docCount.put(docId, read_docCount);
		}
	}

	/**
	 * Clears map to avoid repeats
	 * 
	 */
	public void clear() {
		mappingResult.clear();
		docCount.clear();
		totalCount = 0;
	}
	
	/**
	 * Combines all occurrences in other results' map 
	 * If and entry does not exist create an entry and put the entry to current map 
	 * else combine current and other result together
	 * 
	 * @param MyResultWritable of other results called from the IndexReducer class
	 * @param context for counter
	 * 
	 */
	public void take(MyResultWritable other, Reducer<Text, MyResultWritable, Text, MyResultWritable>.Context context) {
		for (Integer docID : other.mappingResult.keySet()) {

			if (!mappingResult.containsKey(docID)) {
				mappingResult.put(docID, new ArrayList<Integer>(other.mappingResult.get(docID)));
				this.docCount.put(docID, other.docCount.get(docID));//
				// counter for how many new entries in map
				context.getCounter("MyResultWritable take method", "How many new entries to the map").increment(1);

			} else {
				int this_plus_other_docCount = this.docCount.get(docID) + other.docCount.get(docID);
				docCount.put(docID, this_plus_other_docCount);// update the count for document Count

				this.mappingResult.get(docID).addAll(other.mappingResult.get(docID));
				// counter for how many were combined to one map
				context.getCounter("MyResultWritable take method", "How many results were combined to one map").increment(1);

			}
		}
		this.totalCount += other.totalCount;
	}

	/**
	 * Overloading method
	 * Makes a single entry of a single word and where they occur
	 *
	 * @param which document the word occurs in
	 * @param which line number the word occurs
	 * @param which position in the line the word occurs
	 * 
	 */
	public void take(int idNum, int lineNumber, int wordNumber) {

		clear();
		ArrayList<Integer> singleEntry = new ArrayList<Integer>(2);
		singleEntry.add(lineNumber);
		singleEntry.add(wordNumber);

		mappingResult.put(idNum, singleEntry);
		docCount.put(idNum, 1);
		totalCount = 1;

	}
	
	/**
	 * Override toString() method to format the results to conform to specification
	 * 
	 * @param String of formatted mappingResult object
	 */
	@Override
	public String toString() {

		StringBuilder result = new StringBuilder();
		result.append(totalCount).append("\n");
		for (Integer docID : mappingResult.keySet()) {
			result.append("\t").append(docID).append(" ").append(docCount.get(docID));
			result.append("\n");
			ArrayList<Integer> array = mappingResult.get(docID);
			for (int i = 0; i < mappingResult.get(docID).size(); i += 2) {
				result.append("\t\t").append(array.get(i));
				result.append(" ").append(array.get(i + 1)).append("\n");
			}
		}
		return result.toString();

	}


}

package invertedindex;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;

//This class is no longer in use

public class MyMapWritable implements Writable {

	public int document;
	public int linePosition;
	public int wordPosition;

	/**
	 * Override method to write the results
	 * 
	 * @param DataOutput of what to write out
	 */
	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		out.writeInt(document);
		out.writeInt(linePosition);
		out.writeInt(wordPosition);
	}
	
	/**
	 * Override method to read the results that where written
	 * 
	 * @param DataInput of what to read in
	 */
	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		document = in.readInt();
		linePosition = in.readInt();
		wordPosition = in.readInt();

	}

}
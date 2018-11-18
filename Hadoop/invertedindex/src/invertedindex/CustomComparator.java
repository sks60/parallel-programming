package invertedindex;

import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Text;

public class CustomComparator implements RawComparator<Text>{

	/**
	 * Custom comparator class to sorts words by length in a descending order
	 * 
	 * @param Text o1 
	 * @param Text o2
	 * 
	 * @return integer -1 || 0 || 1 
	 */
	@Override
	public int compare(Text o1, Text o2) {
		if(o1.getLength() > o2.getLength()) {
			return -1; //-1 to not change
		}else if(o1.getLength() < o2.getLength()) {
			return 1;
		}else {
			return o1.compareTo(o2);
		}
		
	}

	/**
	 * Custom comparator class method (overloaded) to sorts words 
	 * by length in a descending order if in byte form
	 * 
	 * @param byte[] b1 
	 * @param int s1
	 * @param int l1
	 * @param byte[] b2
	 * @param int s2
	 * @param int l2
	 * 
	 * @return integer -1 || 0 || 1
	 */
	@Override
	public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {

		if(l1 > l2) {
			return -1;
		}else if(l1 < l2) {
			return 1;
		}else {
			return Text.Comparator.compareBytes(b1, s1, l1, b2, s2, l2);
		}

	}

}

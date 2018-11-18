package invertedindex;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


public class IndexMapper extends Mapper<LongWritable, Text, Text, MyResultWritable> {
	private Text word = new Text();
	private MyResultWritable positions = new MyResultWritable();

	/**
	 * Mapper to take documents and create their line and word occurrence number 
	 * 
	 * @param key (word)
	 * @param MyResultWritable values as an Iterable 
	 * @param Context
	 */
	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		
		String document = value.toString();//make the input into a string
		StringTokenizer lineTokenizer = new StringTokenizer(document, "\n");//create tokens by new line
		
		String stopWords = context.getConfiguration().get("stopWords");//get stopwords string
		Set<String> stop_Word = new HashSet<String>();
		//split the words by space to make into one letter/word
		for(String word: stopWords.split(" ")) {
			stop_Word.add(word);
		}
		
		//the first line contains the document id 
		String tag = lineTokenizer.nextToken();
		//extract the id number and assign to idNum
		int idNum = Integer.parseInt(tag.substring(tag.indexOf("\"")+1, tag.indexOf("\"", tag.indexOf("\"")+1)));

		int lineNumber = 0;
		int wordNumber = 0;
		while (lineTokenizer.hasMoreTokens()) { //while there are more lines continue

			StringTokenizer wordTokenizer = new StringTokenizer(lineTokenizer.nextToken());

			while (wordTokenizer.hasMoreTokens()) { //while there are more words continue
				
				String currentWord = wordTokenizer.nextToken();
				
				if(!stop_Word.contains(currentWord)) { 
				
					word.set(currentWord);
				
					//Counter for how many words are used
					context.getCounter("IndexMapper","How many words in Documents").increment(1);
					//Counter for how many words start with A
					if(currentWord.startsWith("A")) context.getCounter("IndexMapper","How many words start with A").increment(1);
				
					positions.take(idNum, lineNumber, wordNumber);
					context.write(word, positions);
				}
				
				wordNumber++;
			}
			lineNumber++;
			context.getCounter("IndexMapper","How many lines seen").increment(1);
			wordNumber = 0;
		}
	}
}

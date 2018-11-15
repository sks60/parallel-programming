

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * A class provides stream assignment implementation template
 * 
 */
public class StreamAssignmentTemplate {

	
	/**
	 * @param file: a file used to create the word stream
	 * @return a stream of word strings
	 * Implementation Notes:
	 * This method reads a file and generates a word stream. 
	 * In this exercise, a word only contains English letters (i.e. a-z and A-Z), and
	 * consists of at least one letter. For example, “The” or “tHe” is a word, 
	 * but “89B” (containing digits 89) or “things,”
     * (containing the punctuation ",") is not. 
	 */ 	
	public static Stream<String> toWordStream(String file){
		
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader( new FileReader(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final long time2 = System.nanoTime();
		Stream<String> testParallel = reader.lines().parallel()
				.flatMap(line -> Stream.of(line.split(" ")))
				.filter(word -> word.matches("[a-zA-Z]+"));
		final long time3 = System.nanoTime();
		System.out.println("To Word Stream(Parallel): " + (time3 - time2) / 1000 + " Mircoseconds");
		
		return testParallel;
	}
	
	/**
	 * @param file: a file used to create a word stream
	 * @return the number of words in the file
	 * Implementation Notes:
	 * This method 
	 *   (1) uses the toWordStream method to create a word stream from the given file
	 *   (2) counts the number of words in the file
	 *   (3) measures the time of creating the stream and counting  
	 * 
	 */
	public static long wordCount(String file){
		
		final long time0 = System.currentTimeMillis();
		long result = toWordStream(file).count();
		final long time1 = System.currentTimeMillis();
		
		System.out.println("Word Count: " + (time1 - time0) / 1e3 + " seconds");
				
		return result;
	}
	
	/**
	 * @param file: a file used to create a word stream
	 * @return a list of the unique words, sorted in a reverse alphabetical order.
	 * Implementation Notes:
	 * This method 
	 *   (1) uses the toWordStream method to create a word stream from the given file
	 *   (2) generates a list of unique words, sorted in a reverse alphabetical order
	 * 
	 */
	public static List<String> uniqueWordList(String file){
		
		List<String> result = toWordStream(file)
										.distinct()
										.sorted(Comparator.reverseOrder())
										.collect(Collectors.toList());
		System.out.println(result.size());
		return result;
	}
	
	/**
	 * @param file: a file used to create a word stream
	 * @return one of the longest words in the file
	 * Implementation Notes:
	 * This method 
	 *   (1) uses the toWordStream method to create a word stream from the given file
	 *   (2) uses Stream.reduce to find the longest word
	 * 
	 */
	public static String longestWord(String file){
		
		//https://stackoverflow.com/questions/46341610/show-all-the-longest-words-from-finite-stream
		//https://stackoverflow.com/questions/35384145/system-out-println-of-streamreduce-unexpectedly-prints-optional-around
		String result = toWordStream(file)
										.reduce(" ", (s1, s2) -> {
											if (s1.length() > s2.length())
												return s1;
											else
												return s2;
										});
		
		return result;
	}
	

	/**
	 * @param file: a file used to create a word stream
	 * @return the number of words consisting of three letters
	 * Implementation Notes:
	 * This method 
	 *   (1) uses the toWordStream method to create a word stream from the given file
	 *   (2) uses Stream.reduce (NO other stream operations) 
	 *       to count the number of words containing three letters.
	 *       i.e. Your code will look like: 
     *       return toWordStream(file).reduce(...); 
     *       
	 */
	public static long wordsWithThreeLettersCount(String file){
		
		Long result = toWordStream(file).reduce(0L, (tally, word) -> { 
														if (word.length() == 3) { 
															return tally += 1; 
														} else { 
															return tally; 
														}
													}, (tally1, tally2) -> tally1 + tally2);
		return result;
	}
	
	/**
	 * @param file: a file used to create a word stream
	 * @return the average length of the words (e.g. the average number of letters in a word)
	 * Implementation Notes:
	 * This method 
	 *   (1) uses the toWordStream method to create a word stream from the given file
	 *   (2) uses Stream.reduce (NO other stream operations) 
	 *       to calculate the total length and total number of words  
     *   (3) the average word length can be calculated separately i.e. total_length/total_number_of_words 
	 */
	public static double avergeWordlength(String file){
		double[] sumCount = toWordStream(file).reduce(new double[]{0.0,0.0}, (array, nextWord) -> {
																array[0] += nextWord.length();
																array[1] += 1;
																return array;
															}, (x,y) -> {
																x[0] += y[0];
																x[1] += y[1];
																return x;
															});
		double average = sumCount[0] / sumCount[1];
	
		return average;
	}
	
	/**
	 * @param file: a file used to create a word stream 
	 * @return a map contains key-value pairs of a word (i.e. key) and its occurrences (i.e. value)
	 * Implementation Notes:
	 * This method 
	 *   (1) uses the toWordStream method to create a word stream from the given file
	 *   (2) uses Stream.collect, Collectors.groupingBy, etc., to generate a map 
	 *        containing pairs of word and its occurrences.
	 */
	public static Map<String,Integer> toWordCountMap(String file){
		//for integer https://stackoverflow.com/a/25512294
		Map<String, Integer> stringMap = toWordStream(file)
				.collect(Collectors.groupingBy(s -> s.toString(), Collectors.reducing(0, e -> 1, Integer::sum)));

      	return stringMap;
	}
	
	/**
	 * @param file: a file used to create a word stream 
	 * @return a map contains key-value pairs of a letter (i.e. key) and a set of words starting with that letter (i.e. value) 
	 * Implementation Notes:
	 * This method 
	 *   (1) uses the toWordStream method to create a word stream from the given file
	 *   (2) uses Stream.collect, Collectors.groupingBy, etc., to generate a map containing pairs of a letter
	 *    and a set of words starting with that letter 
	 * 
	 */
	public static Map<String,Set<String>> groupWordByFirstLetter(String file){
		
		Map<String, Set<String>> group = toWordStream(file)
										.collect(Collectors.groupingBy(s -> String.valueOf(s.charAt(0)), Collectors.toSet()));
		
		return group;
	}
	
	
	/**
	 * @param BiFunction that takes two parameters (String s1 and String s2) and 
	 *        returns the index of the first occurrence of s2 in s1, or -1 if s2 is not a substring of s1
	 * @param targetFile: a file used to create a line stream
	 * @param targetString:  the string to be searched in the file
	 *  Implementation Notes:
	 *  This method
	 *   (1) uses BufferReader.lines to read in lines of the target file
	 *   (2) uses Stream operation(s) and BiFuction to 
	 *       produce a new Stream that contains a stream of Object[]s with two elements;
	 *       Element 0: the index of the first occurrence of the target string in the line
	 *       Element 1: the text of the line containing the target string
	 *   (3) uses Stream operation(s) to sort the stream of Object[]s in a descending order of the index
	 *   (4) uses Stream operation(s) to print out the first 20 indexes and lines in the following format
	 *           567:<the line text>
	 *           345:<the line text>
	 *           234:<the line text>
	 *           ...  
	 */
	public static void printLinesFound(BiFunction<String, String, Integer> pf, String targetFile, String targetString) {

		
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(targetFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final long time2 = System.nanoTime();
		Stream<Object[]> lines = reader.lines().parallel()
									.map(line -> {
													Object[] result = new Object[2];
													result[0] = pf.apply(line, targetString); //apply the lambda in parameter sent
													result[1] = line; //this is the line in which it is found
													return result;
													})
									.sorted((i1, i2) -> { //sort in descending order 
															if ((Integer) i1[0] > (Integer) i2[0]) 
																return -1;
															else if((Integer) i1[0] < (Integer) i2[0])
																return 1;
															else
																return 0;
															})
									.limit(20);
		//lines.forEach(x -> System.out.println(x[0] + ": " + x[1]));//for not parallel 
		lines.forEachOrdered(x -> System.out.println(x[0] + ": " + x[1])); // for parallel to keep the final joined streams ordered
		final long time3 = System.nanoTime();

		System.out.println("printLinesFound: " + (time3 - time2) / 1000000 + " Milliseconds");
	}
	
	 public static void main(String[] args) throws IOException{
		  //test your methods here;
		 
		 //System.out.println(toWordStream("wiki_1.xml"));
		 //System.out.println(wordCount("wiki_1.xml"));
		 //System.out.println(uniqueWordList("wiki_1.xml"));
		 //System.out.println(longestWord("wiki_1.xml"));
		 //System.out.println(wordsWithThreeLettersCount("wiki_1.xml"));
		 //System.out.println(toWordCountMap("wiki_1.xml"));
		 //System.out.println(avergeWordlength("wiki_1.xml"));
		 //System.out.println(groupWordByFirstLetter("wiki_1.xml"));
		 //printLinesFound((s1, s2) -> s1.indexOf(s2), "wiki_1.xml", "science");
	 }

		
	
	
	
	
	
}

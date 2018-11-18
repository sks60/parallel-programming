package completablefuture_assignment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class that provides personal details (i.e., fullname, birth year and gender)
 * @author shaoqun Wu
 *
 */
public class PersonInfoSupplier {
	    //a list to store PersonInfo objects
		static List<PersonInfo> infoList;
		
		//read in all personal info from a file
		static{
			try{
				 BufferedReader br = new BufferedReader(new FileReader("fullnames"));
				 infoList = br.lines()
				              .map(line -> {
				                  String[] parts = line.split(",");
				                	  return new PersonInfo(parts[0],parts[1],Integer.parseInt(parts[2]));  
				                 })
			                  .collect(Collectors.toList());
				 br.close();
			 }
			 catch(Exception e){
			     e.printStackTrace();
		      } 
		}
		private static Random random = new Random();
		
 		private static void delay(){
			 try {
			  	  //delay a bit
		         Thread.sleep(random.nextInt(10)*100);		                   
		         } catch (InterruptedException e) {
		         throw new RuntimeException(e); }
		}
				
	
	 /**
	  *A supplier that returns personal info given a fullname
	  * @param fullname ("firstname lastname")
	  * @return Returns an Optional describing the first PersonInfo object matching the given name, 
	  *         or an empty Optional if not found
	  */
	 public static Optional<PersonInfo>  getPersonInfo(String fullname){
		 String currentThreadName = Thread.currentThread().getName();
	    if(Mortality.debug){
		   //System.out.println("&&"+currentThreadName + "&& retrieving "+fullname+"'s birth year and gender...." );
		 }
		delay();
		Optional<PersonInfo> personInfoOp = infoList.parallelStream().filter(personInfo->personInfo.match(fullname)).findFirst();
		if(Mortality.debug){
		  //System.out.println("&&"+currentThreadName + "&& returned "+fullname+"'s birth year and gender");
		}
		return personInfoOp;
		    	  	
	 }
   }	




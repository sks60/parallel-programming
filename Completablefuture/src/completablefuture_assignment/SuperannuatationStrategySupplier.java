package completablefuture_assignment;

import java.util.Random;
import java.util.stream.IntStream;


/**
 * A class that provides superannuatation details
 * @author Shaoqun Wu
 *
 */
public class SuperannuatationStrategySupplier {
	/*
	 * Four superannuatation strategies
	 */
	final static String[] superStrategies = new String[]{
  			 "growth", "balanced","conservative", "cash"};
     
    /*
     * puts the current thread to sleep for a few seconds.
     */
	protected static void delay(){
       try {
			  	  //delay a bit
    	             Random random = new Random();  
		         Thread.sleep(random.nextInt(10)*100);		                   
		         } catch (InterruptedException e) {
		         throw new RuntimeException(e); }
		}

	  /*
	   * @return a start super age between 25 to 50 (inclusive)
	   */
      public static int getStartSuperAge(){
   	   String currentThreadName = Thread.currentThread().getName();
   	  
    	  if(Mortality.debug){
   	     //System.out.println("=="+currentThreadName + "== retrieving a start super age...");
    	    }    
   	     int startSuperAge = 25 + new Random().nextInt(16);
   	  if(Mortality.debug){
   	     //System.out.println("=="+currentThreadName + "== returned a start super age: "+startSuperAge);
   	    }
   	     return startSuperAge;
       }
      
      /*
       *@return the name (string) of the superannuation investment strategy that you wish to use. 
       * 
       */
      public static String getSuperStrategy(){
 	   String currentThreadName = Thread.currentThread().getName();
 	    
    	  if(Mortality.debug){
    	     //System.out.println("**"+currentThreadName + "** retrieving strategy ...");
    	    }
         String strategy  = superStrategies[new Random().nextInt(superStrategies.length)];
         if(Mortality.debug){
           //System.out.println("**"+currentThreadName + "** returned strategy: "+ strategy);
         }
         return strategy;
   	   
      }
     /*
      *@return  the (integer) percentage (between 5 to 15 (inclusive)) of your salary that you will put into your super each year. 
      */
     public static int getContribution(){
    	 String currentThreadName = Thread.currentThread().getName();
    	 if(Mortality.debug){
   	    //System.out.println("##"+currentThreadName + "## retrieving contribution percentage ....");
    	   }
        int conPer = 5 + new Random().nextInt(16);
        if(Mortality.debug){
         // System.out.println("##"+currentThreadName + "## returned contribution percentage:"+conPer);
        }
         return conPer;   
     }    
	
	
	
}

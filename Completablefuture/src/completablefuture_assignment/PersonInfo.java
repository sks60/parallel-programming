package completablefuture_assignment;


/*
* a wrapper class that holds personal info (i.e., name, birth year and gender)
*/
public class PersonInfo {
	String name;
	int birthYear;
    String gender; 
   
   public PersonInfo(String name,  String gender,int birthYear){
   	  this.name = name;
   	  this.gender = gender;
   	  this.birthYear = birthYear;
   }
   
   protected boolean match(String name){
   	  return name.equals(this.name);
   }
   
   public int getBirthYear(){
	   return this.birthYear;
   }
   
   public String getGender(){
	   return this.gender;
  }
   
   public String toString(){
	   return name + "," + gender+ "," + birthYear;
   }
}


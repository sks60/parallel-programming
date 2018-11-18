// Databricks notebook source


// COMMAND ----------

// MAGIC %md #####Note: You have the freedom to use SQL statements, or DataFrame/Dataset operations to do the tasks 

// COMMAND ----------

// MAGIC %md (2 marks) Task 1. Read in the "Film Locations in San Francisco" data file and make it to a DataFrame called sfflDF.

// COMMAND ----------

//write your code for task 1 here
val sffIDF = sqlContext.read
                       .format("csv")
                       .option("header", "true")
                       .option("inferSchema", "true")
                       .load("dbfs:/FileStore/tables/Film_Locations_in_San_Francisco.csv")

// COMMAND ----------

// MAGIC %md (2 marks) Task 2. Print the schema of sfflDF and show the first ten records.

// COMMAND ----------

//write your code for task 2 here
display(sffIDF.take(10))

// COMMAND ----------

// MAGIC %md (1 marks) Task 3. Count the number of records in sfflDF.

// COMMAND ----------

//write your code for task 3 here
sffIDF.count

// COMMAND ----------

// MAGIC %md (4 marks) Task 4. Filter the records where "director" is null, or there two or more director names (indicated by "and" or "&") in the "director" column, or "locations" is null, or "release year" is not a number (i.e. containing non-digit characters), and call the new DataFrame fsfflDF. 

// COMMAND ----------

//write your code for task 4 here
import org.apache.spark.sql.functions._
import org.apache.spark.sql._
import org.apache.spark.sql.types._

val fsffIDF = sffIDF
                    .filter($"Director".isNotNull &&
                            !$"Director".contains(" and ") && !$"Director".contains("&") &&
                            $"Locations".isNotNull)
                    .filter("CAST(`Release Year` AS INTEGER) IS NOT NULL") //taken from https://stackoverflow.com/a/51486182

display(fsffIDF)

// COMMAND ----------

// MAGIC %md (2 marks) Task 5. Add a new column called "trimed_title" to fsfflDF that contains the title of films with the space trimed from the left and right end, drop the old "title" column and rename "trimed_title" column to "title", and call this new DataFrame csfflDF. 

// COMMAND ----------

//write your code for task 5 here
val csffIDF = fsffIDF.withColumn("Title", trim(fsffIDF("title"))) 

display(csffIDF)

// COMMAND ----------

// MAGIC %md #####Note: You will use csfflDF  in the following tasks

// COMMAND ----------

// MAGIC %md (2 marks) Task 6. Show the title, release year of films that were released between 2000-2009 (inclusive), ordered by the release year (from latest to earliest).

// COMMAND ----------

//write your code for task 6 here
val filterYear = csffIDF.select("Title", "Release Year").distinct
                        .where($"Release Year" >= 2000 && $"Release Year" <= 2009)
                        .sort(desc("Release Year"))

display(filterYear)

// COMMAND ----------

// MAGIC %md (2 marks) Task 7. Show the title of film(s) written by Keith Samples (note: there could be more than one writer name in the "writer" column)

// COMMAND ----------

//write your code for task 7 here
val keith_samples = csffIDF.select("Title", "Writer")
                           .filter($"Writer".contains("Keith Samples"))

display(keith_samples)

// COMMAND ----------

// MAGIC %md (2 marks) Task 8. Show the earliest and latest release year. 

// COMMAND ----------

//write your code for task 8 here
val early_late = csffIDF.select(min($"Release Year"), max($"Release Year"))

display(early_late)

// COMMAND ----------

// MAGIC %md (3 marks) Task 9. Count the number of films, the number of distinct production company, and the average number of films made by a production company

// COMMAND ----------

//write your code for task 9 here
val num_films = csffIDF.select("Title").distinct.count

val num_productionCompany = csffIDF.select("Production Company").distinct.count

//I have removed duplicates of the titles (because of multiple locations) to get proper count of distinct films to get the accurate average
val average_filmsByCompany = csffIDF.groupBy("Production Company")
                                    .agg(countDistinct("Title").as("x"))
                                    .agg(mean("x")).show()


// COMMAND ----------

// MAGIC %md (3 marks) Task 10. Show the title of films that were shot at more than three locations (inclusive).  

// COMMAND ----------

//write your code for task 10 here
//I have removed duplicates of the titles (because of multiple locations) to get proper count of distinct films
val title_location = csffIDF.groupBy("Title")
                            .agg(countDistinct("Locations").as("Greater than 3"))
                            .filter($"Greater than 3" >= 3 )

display(title_location)

// COMMAND ----------

// MAGIC %md (3 marks) Task 11. Add a new column called "Crew" to csfflDF that contains a list of people who had worked on the film (i.e. concatenate the column "Actor 1", "Actor 2", "Actor 3", "Director", "Distributor", Writer"). Show the title of films that Clint Eastwood were involved.

// COMMAND ----------

//write your code for task 11 here
val crew = csffIDF.withColumn("Crew", concat_ws(", ", $"Actor 1", $"Actor 2", $"Actor 3", $"Director", $"Distributor", $"Writer"))
               .select("Title").distinct
               .filter($"Crew".contains("Clint Eastwood"))

display(crew)

// COMMAND ----------

// MAGIC %md (3 marks) Task 12. Show the number of films directed by each director, order by the number of films (descending).

// COMMAND ----------

//write your code for task 12 here
//I have removed duplicates of the titles (because of multiple locations) to get proper count of distinct films
val film_director = csffIDF.groupBy("Director")
                           .agg(countDistinct("Title").as("film"))
                           .sort(desc("film"))

display(film_director)

// COMMAND ----------

// MAGIC %md (3 marks) Task 13. Show the number of films made by each production company and in each release year.

// COMMAND ----------

//write your code for task 13 here
//I have removed duplicates of the titles (because of multiple locations) to get proper count of distinct films
val film_production_year = csffIDF.groupBy("Production Company", "Release Year")
                                  .agg(countDistinct("Title").as("Count"))
                                  .sort(desc("Count"))

display(film_production_year)

// COMMAND ----------

// MAGIC %md (3 marks) Task 14. Use csfflDF to generate a new DataFrame that has the column "title" and a new column "location_list" that contains an array of locations where the film was made, and then add a new column that contains the number of locations in "location_list". Show the first ten records of this dataframe.  

// COMMAND ----------

//write your code for task 14 here
val newDF = csffIDF.groupBy("Title")
                   .agg(collect_set($"Locations").as("Location List"), count("Locations").as("Number of Location in List"))
                   .show(10)


// COMMAND ----------

// MAGIC %md (3 marks) Task 15. Use csfflDF to generate a new DataFrame that has the "title" column, and two new columns called "Golden Gate Bridge" and "City Hall" that contains the number of times each location was used in a film. Show the title of films that had used each location at least once.
// MAGIC Hint: Use pivot operation  

// COMMAND ----------

//write your code for task 15 here
val pivot = csffIDF.groupBy("Title")
                   .pivot("Locations", Seq("Golden Gate Bridge", "City Hall")).count
                   .filter($"Golden Gate Bridge" >= 1 && $"City Hall" >= 1)

display(pivot)

// COMMAND ----------

// MAGIC %md (4 marks) Task 16. Add a new column to csfflDF that contains the names of directors with their surname uppercased.
// MAGIC Hint. Use a user defined function to uppercase the director's surname. Please refer to https://jaceklaskowski.gitbooks.io/mastering-apache-spark/content/spark-sql-udfs.html

// COMMAND ----------

//write your code for task 16 here

/*
* This function is for a simple case where there is only one director in the column
* will not work if there are more than one director in the column
* such as "Jamie Babbit, Amanda Brotchie, Steven K. Tsuchida, Christian Ditter, John Riggi"
*/
val upperLastName = udf { s: String => 
  val spaceIndex = s.indexOf(" ")+1
  val firstName = s.substring(0, spaceIndex)
  val lastName = s.substring(spaceIndex, s.length()).toUpperCase
  if(spaceIndex < 1){
    s
  }else{
    firstName.concat(lastName)
  }
}

val upperLN = csffIDF.select("Director").distinct
                     .withColumn("Upper-Cased Last Name", upperLastName($"Director"))

display(upperLN)

// COMMAND ----------

// MAGIC %md (4 marks) Task 17. Use csfflDF to generate a new DataFrame that has the column "Locations" and a new column "Actors" that contains a list of distinct actors who had cast in a location. 
// MAGIC Note: run "Hint" below if you need a bit help on this task  

// COMMAND ----------

//write your code for task 17 here
val combineArray = udf {(x: Seq[String], y: Seq[String], z: Seq[String]) =>
  x++y++z
}

val locations_actors = csffIDF.groupBy("Locations")
                              .agg(collect_set($"Actor 1").as("A1"), collect_set($"Actor 2").as("A2"), collect_set($"Actor 3").as("A3"))
                              .withColumn("Actors", combineArray(col("A1"), col("A2"), col("A3")))
                              .select("Locations", "Actors")

display(locations_actors)


// COMMAND ----------

displayHTML("<p>One solution is groupBy on <i>Locations</i>, then collect_set on <i>Actor 1</i>, <i>Actor 2</i>, <i>Actor 3</i>, finally define a user function to combine <i>collect_set(Actor 1)</i>, <i>collect_set(Actor 2)</i>,  <i>collect_set(Actor 3)</i></p>")

// COMMAND ----------

// MAGIC %md (4 marks) Task 18. Show the names of people who is an actor (in one of "Actor 1", "Actor 2" and "Actor 3" column) and also a director (in the "Director" column). Note: run "Hint" below if you need a bit help on this task

// COMMAND ----------

//write your code for task 18 here
//The method below is only for displaying actors that are also directors.
//This does not show the same person who acts and directs in the same film.
val a1 = csffIDF.select($"Actor 1".alias("Actor")).distinct
                .where($"Actor 1".isNotNull)
val a2 = csffIDF.select($"Actor 2".alias("Actor")).distinct
                .where($"Actor 2".isNotNull)
val a3 = csffIDF.select($"Actor 3".alias("Actor")).distinct
                .where($"Actor 3".isNotNull)
val director = csffIDF.select("Director").distinct
                      .where($"Director".isNotNull)

val unionAll = a1.union(a2)
                 .union(a3)

val actor_director = unionAll.join(director, $"Actor".contains($"Director")).select("Actor")

display(actor_director)


// COMMAND ----------

displayHTML("<p>One solution is use csfflDF to create four new DataFrames (for 'Actor 1', 'Actor 2', 'Actor 3' and 'Director') that contains two columns, the actor/director name and the film title, and then use 'union' and 'join' opetations.</p>")

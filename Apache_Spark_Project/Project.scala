// Databricks notebook source
displayHTML("<h1>Project: Machine Learning with Car Evaluation Dataset</h1><p>**This project follows the MLlib Demo Notebook provided on the Moodle for COMP553**</p><h3>Predict the acceptability of a car based on Buying Price, Maintenance, Number of Doors, Number of People it carries, Boot Space, and its Safety. </h3>"+
"<p>Dataset Car Evaluation from https://archive.ics.uci.edu/ml/datasets/Car+Evaluation</p>"+
"<p>Chosen to use the Spark MLlib</p>")

// COMMAND ----------

displayHTML("<h2>1. Creating DataFrames</h2><p>Imported the Car Evaluation Dataset and renamed the columns.</p>")

// COMMAND ----------

import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.classification.DecisionTreeClassificationModel
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{OneHotEncoder, StringIndexer, VectorAssembler}

val dataDF = spark.read.format("csv").load("/FileStore/tables/car.csv")
val newNames= Seq("buying","maintain", "doors", "persons", "boot", "safety", "label")
val dfRenamed = dataDF.toDF(newNames: _*)

// COMMAND ----------

displayHTML("<h2>2. Create Features Table</h2>")

// COMMAND ----------


//Encode and transfor the dataset to get vector form for DecisionTreeClassifier
//---------------------------Encode buying column--------------------------------------
val indexerB = new StringIndexer().setInputCol("buying").setOutputCol("buying_2").fit(dfRenamed)
val transformIndexB = indexerB.transform(dfRenamed)
val encoderB = new OneHotEncoder().setInputCol("buying_2").setOutputCol("buying_3")
val tansformEncoderB = encoderB.transform(transformIndexB)
//-------------------------------------------------------------------------
//------------------------------Encode maintain column--------------------------------
val indexerM = new StringIndexer().setInputCol("maintain").setOutputCol("maintain_2").fit(tansformEncoderB)
val transformIndexM = indexerM.transform(tansformEncoderB)
val encoderM = new OneHotEncoder().setInputCol("maintain_2").setOutputCol("maintain_3")
val tansformEncoderM = encoderM.transform(transformIndexM)
//-------------------------------------------------------------------------
//------------------------------Encode doors column----------------------
val indexerD = new StringIndexer().setInputCol("doors").setOutputCol("doors_2").fit(tansformEncoderM)
val transformIndexD = indexerD.transform(tansformEncoderM)
val encoderD = new OneHotEncoder().setInputCol("doors_2").setOutputCol("doors_3")
val tansformEncoderD = encoderD.transform(transformIndexD)
//-------------------------------------------------------------------------
//------------------------------Encode persons column-------------------------
val indexerP = new StringIndexer().setInputCol("persons").setOutputCol("persons_2").fit(tansformEncoderD)
val transformIndexP = indexerP.transform(tansformEncoderD)
val encoderP = new OneHotEncoder().setInputCol("persons_2").setOutputCol("persons_3")
val tansformEncoderP = encoderP.transform(transformIndexP)
//-------------------------------------------------------------------------
//------------------------------Encode boot column--------------------------
val indexerBo = new StringIndexer().setInputCol("boot").setOutputCol("boot_2").fit(tansformEncoderP)
val transformIndexBo = indexerBo.transform(tansformEncoderP)
val encoderBo = new OneHotEncoder().setInputCol("boot_2").setOutputCol("boot_3")
val tansformEncoderBo = encoderBo.transform(transformIndexBo)
//-------------------------------------------------------------------------
//--------------------------------Encode safety column--------------------------
val indexerS = new StringIndexer().setInputCol("safety").setOutputCol("safety_2").fit(tansformEncoderBo)
val transformIndexS = indexerS.transform(tansformEncoderBo)
val encoderS = new OneHotEncoder().setInputCol("safety_2").setOutputCol("safety_3")
val tansformEncoderS = encoderS.transform(transformIndexS)
//-------------------------------------------------------------------------

val vectors = new VectorAssembler().setInputCols(Array("buying_3","maintain_3", "doors_3", "persons_3", "boot_3","safety_3")).setOutputCol("features")
val tansformVectors = vectors.transform(tansformEncoderS)

val indexerString = new StringIndexer().setInputCol("label").setOutputCol("label_2").fit(tansformVectors)
val finalTransform = indexerString.transform(tansformVectors)

display(finalTransform.select("features"))

// COMMAND ----------

displayHTML("<h3>Define the split for training and testing. 70% will be used for training and the 30% will be used to test the model</h3>")

// COMMAND ----------

val Array(trainingData, testData) = finalTransform.randomSplit(Array(0.7, 0.3))
println(trainingData.count())

// COMMAND ----------

displayHTML("<h2>3. Create the model</h2>")

// COMMAND ----------

val dt = new DecisionTreeClassifier()
  .setLabelCol("label_2")
  .setFeaturesCol("features")

val pipeline = new Pipeline()
  .setStages(Array(dt))

val model = pipeline.fit(trainingData)

// COMMAND ----------

displayHTML("<h2>4. Prediction</h2>")

// COMMAND ----------

val predictions = model.transform(testData)
display(predictions.select("buying","maintain", "doors", "persons", "boot", "safety", "label_2", "prediction"))

// COMMAND ----------

displayHTML("<h2>5. Evaluate the accuracy</h2>")

// COMMAND ----------

val evaluator = new MulticlassClassificationEvaluator()
  .setLabelCol("label_2")
  .setPredictionCol("prediction")
  .setMetricName("accuracy")
val accuracy = evaluator.evaluate(predictions)
println("Test Error = " + (1.0 - accuracy))
println("Accuracy: " + accuracy)

package example

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

object Main extends App {
  // Hide log information
  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("com").setLevel(Level.OFF)
  System.setProperty("spark.ui.showConsoleProgress","false")
  Logger.getRootLogger.setLevel(Level.OFF)

  println("Running WordCount")
  val sc = new SparkContext (new SparkConf().setAppName("wordCount").setMaster("local[4]"))
  println("Reading file")
  val textFile = sc.textFile("data/LICENSE.txt")
  println("Creating RDD")
  val countsRDD = textFile.flatMap(line => line.split(" "))
    .map(word => (word, 1))
    .reduceByKey(_ + _)
  println("Saving file")
  try {
    countsRDD.saveAsTextFile("data/output")
    println("Saved successfully")
  }  catch {
    case e: Exception => println("The output file already exists, clear the contents of the output directory");
  }
  println("Task done")
}

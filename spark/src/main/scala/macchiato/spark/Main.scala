package macchiato.spark

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

object Main {

  def main(args: Array[String]) {
    println("Macchiato Spark start...")

    val file = "README.md"

    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("Macchiato")
    //.setSparkHome("/opt/spark-0.9.0-incubating-bin-hadoop2")
    //.setJars(List("target/scala-2.10/macchiato-spark_2.10-1.0.jar"))

    val sc = new SparkContext(conf)

    val data = sc.textFile(file, 2).cache()
    val numAs = data.filter(line => line.contains("a")).count()
    val numBs = data.filter(line => line.contains("b")).count()

    println("Lines with a: %s, Lines with b: %s".format(numAs, numBs))

    sys.ShutdownHookThread {
      println("Macchiato Spark shutdown...")
    }
  }

}

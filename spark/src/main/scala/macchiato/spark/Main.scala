package macchiato.spark

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

object Main {

  def main(args: Array[String]) {
    println("Macchiato Spark start...")

    val file = "README.md"

    val conf = new SparkConf()
      .setMaster("local[4]")
      .setAppName("Macchiato")

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

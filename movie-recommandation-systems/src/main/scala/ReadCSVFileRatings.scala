import org.apache.spark.sql.functions.{countDistinct, mean}
import org.apache.spark.sql.types.FloatType
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

// read csv file
object ReadCSVFileRatings {

  case class ratings(userId: String, movieId: String, rating: Float)
  // userId: unique ID for each user
  // movieId: unique ID for each movie
  // rating: ratings are float in [0,5], which 0 is the lowest rating and 5 is best rating
  // timestamp: will not be used in current project; current code still contains timestamp column

  def main(args: Array[String]): Unit = {
    var conf = new SparkConf().setAppName("Read CSV File").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    val ratingsDF= sqlContext.read.format("csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load("/Users/jchen/IdeaProjects/movie-recommandation-systems/src/main/resources/ratings.csv")
    ratingsDF.show(5) //preview

    // number of distinct user
    println("distinct user in data")
    ratingsDF.agg(countDistinct("userId") as "numb of user").show()

    // number of movie being rated
    println("distinct movie in data")
    ratingsDF.agg(countDistinct("movieId") as "number of movie").show()

    // show some statistics of ratings data distribution
    // mean
    println("mean")
    ratingsDF.select(mean("rating") as "mean").show()

    // median
    val median = ratingsDF.withColumn("units", ratingsDF("rating").cast(FloatType)).select("units").stat.approxQuantile("units", Array(0.5), 0.0001).head
    println("median" + median)

    // mode
    println("freq and mode")
    ratingsDF.groupBy("rating").agg(countDistinct("userId") as "freq").show()

  }
}

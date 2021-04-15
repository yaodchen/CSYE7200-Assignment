import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

// read csv file
object ReadCSVFileMovie {

  case class movie(movieId: String, title: String, genres: String)
  // movieID: unique ID for each movie, primary key
  // title: title of the movie, will be returned in recommandation to user
  // genres: movie genres such as Adventure, Animation, Children or Comedy; multiple genres will be separated by "|" for each movie

  def main(args: Array[String]): Unit = {
    var conf = new SparkConf().setAppName("Read CSV File").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    val movieDF= sqlContext.read.format("csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load("/Users/jchen/IdeaProjects/movie-recommandation-systems/src/main/resources/movies.csv")
    movieDF.show(5) //preview
  }
}
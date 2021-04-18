import org.apache.spark.sql.functions.{countDistinct, mean}
import org.apache.spark.sql.types.FloatType
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

// read csv file
object ReadCSVFileRatings {

  case class ratings(userId: String, movieId: String, rating: Float)
  /**
   *
   * userId       unique ID for each user
   * movieId    unique ID for each movie
   * rating ratings are float in [0,5], which 0 is the lowest rating and 5 is best rating
   * timestamp    will not be used in current project; current code still contains timestamp column
   */
  case class movie(movieId: String, title: String, genres: String)
  /**
   *
   * movieId    unique ID for each movie, primary key
   * title    title of the movie, will be returned in recommendation to user
   * genres movie genres such as Adventure, Animation, Children or Comedy; multiple genres will be separated by "|" for each movie
   */

  def main(args: Array[String]): Unit = {

    var conf = new SparkConf().setAppName("Read CSV File").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    // Import the rating dataset
    val ratingsDF = sqlContext.read.format("csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load("C:\\Users\\Yaodong\\IdeaProjects\\scala_project\\src\\main\\resources\\ratings.csv")
    ratingsDF.show(5) //preview


    // Import the movie dataset
    val movieDF = sqlContext.read.format("csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load("C:\\Users\\Yaodong\\IdeaProjects\\scala_project\\src\\main\\resources\\movies.csv")
    movieDF.show(5) //preview

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

    //sql test
    val spark = SparkSession
      .builder()
      .appName("SparkSQL For Csv")
      .master("local[*]")
      .getOrCreate()

    ratingsDF.createOrReplaceTempView("ratings")
    movieDF.createOrReplaceTempView("movies")

    val sqlPersonDF = spark.sql(
      """
        |WITH max_min_ratings AS
        |(
        | SELECT ratings.movieId,
        |        max(ratings.rating) as max_rating,
        |        min(ratings.rating) as min_rating,
        |        count(distinct(userId)) as user_cnt
        | FROM ratings
        | GROUP BY ratings.movieId
        | )
        |SELECT
        | movies.title,
        | max_min_ratings.max_rating,
        | max_min_ratings.min_rating,
        | max_min_ratings.user_cnt
        |FROM
        | max_min_ratings JOIN movies
        |ON
        | max_min_ratings.movieId = movies.movieId
        |ORDER BY
        | max_min_ratings.user_cnt DESC
    """.stripMargin)
    sqlPersonDF.show()

    sqlPersonDF.show(false)



  }
}

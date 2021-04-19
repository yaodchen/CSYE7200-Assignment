import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.sql.functions.{countDistinct, mean}
import org.apache.spark.sql.types.FloatType
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

import scala.Tuple2

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

    /**
     * Import the dataset as ratingsDF and movieDF
     */
    val ratingsDF = sqlContext.read.format("csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load("C:\\Users\\Yaodong\\IdeaProjects\\scala_project\\src\\main\\resources\\ratings.csv")
    ratingsDF.show(5) //preview


    val movieDF = sqlContext.read.format("csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load("C:\\Users\\Yaodong\\IdeaProjects\\scala_project\\src\\main\\resources\\movies.csv")
    movieDF.show(5) //preview

    /**
     * Count the number of distinct users in ratingsDF
     */
    println("distinct user in data")
    ratingsDF.agg(countDistinct("userId") as "numb of user").show()

    /**
     * Count the number of distinct movies in ratingsDF
     */
    println("distinct movie in data")
    ratingsDF.agg(countDistinct("movieId") as "number of movie").show()


    /**
     * Statistics of ratings data distribution. Mean. Median. Mode
     */
    // mean
    println("mean")
    ratingsDF.select(mean("rating") as "mean").show()

    // median
    val median = ratingsDF.withColumn("units", ratingsDF("rating").cast(FloatType)).select("units").stat.approxQuantile("units", Array(0.5), 0.0001).head
    println("median" + median)

    // mode
    println("freq and mode")
    ratingsDF.groupBy("rating").agg(countDistinct("userId") as "freq").show()

    /**
     * Implement Spark.SQL to perform data exploration
     */
    val spark = SparkSession
      .builder()
      .appName("SparkSQL For Csv")
      .master("local[*]")
      .getOrCreate()

    ratingsDF.createOrReplaceTempView("ratings")
    movieDF.createOrReplaceTempView("movies")


    /**
     * Show the max and min ratings and count the number of user ratings for each movie
     */
    val max_min_count = spark.sql(
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
//    max_min_count.show(5)

    /**
     * Return top 20 active users and their ratings
     */
    val top_20_rate = spark.sql(
      """
        |SELECT ratings.userId,
        |       count(*) as count
        |FROM ratings
        |GROUP BY ratings.userId
        |ORDER BY count desc limit 20
    """.stripMargin)
//    top_20_rate.show()


    /**
     * Return the ratings for a specific user (414 for example)
     */
    val id_414_rating = spark.sql(
      """
        |SELECT ratings.userId,
        |       ratings.movieId,
        |       ratings.rating,
        |       movies.title
        |FROM ratings JOIN movies
        |ON movies.movieId = ratings.movieId
        |WHERE ratings.userId = 414
        |AND ratings.rating > 4
    """.stripMargin)
//    id_414_rating.show()


    /**
     * Split data into train(75%) and test(25%)
     */
    val splits = ratingsDF.randomSplit(Array(0.75, 0.25), seed = 12345L)
    val (trainingData, testData) = (splits(0), splits(1))
    val numTraining = trainingData.count()
    val numTest = testData.count()
    println("Training: " + numTraining + " test: " + numTest)

    // Train dataset preparation
    val ratingsRDD = trainingData.rdd.map(row => {
      val userId = row.getInt(0)
      val movieId = row.getInt(1)
      val ratings = row.getDouble(2)
      Rating(userId, movieId, ratings)
    })

    // Test dataset preparation
    val testRDD = testData.rdd.map(row => {
//      val userId = row.getInt(0)
//      val movieId = row.getString(1)
//      val ratings = row.getString(2)
//      Rating(userId.toInt, movieId.toInt, ratings.toDouble)
      val userId = row.getInt(0)
      val movieId = row.getInt(1)
      val ratings = row.getDouble(2)
      Rating(userId, movieId, ratings)
    })

    /**
     * Alternating Least Square User Product Matrix
     */
    val rank = 20
    val numIterations = 15
    val lambda = 0.10
    val alpha = 1.00
    val block = -1
    val seed = 12345L
    val implicitPrefs = false
    val model = new ALS().setIterations(numIterations).setBlocks(block).setAlpha(alpha)
      .setLambda(lambda)
      .setRank(rank) .setSeed(seed)
      .setImplicitPrefs(implicitPrefs)
      .run(ratingsRDD)

    println("Rating:(UserID, MovieID, Rating)")
    println("----------------------------------")
    val topRecsForUser = model.recommendProducts(414, 10)
    for (rating <- topRecsForUser) {println(rating.toString()) }
    println("----------------------------------")


    def computeRmse(model: MatrixFactorizationModel, data: RDD[Rating], implicitPrefs: Boolean): Double = {
      val predictions: RDD[Rating] = model.predict(data.map(x => (x.user, x.product)))
      val predictionsAndRatings = predictions.map { x => ((x.user, x.product), x.rating) }.join(data.map(x => ((x.user, x.product), x.rating))).values
      if (implicitPrefs) { println("(Prediction, Rating)")
        println(predictionsAndRatings.take(5).mkString("n")) }
      math.sqrt(predictionsAndRatings.map(x => (x._1 - x._2) * (x._1 - x._2)).mean()) }

    val rmseTest = computeRmse(model, testRDD, true)
    println("Test RMSE: = " + rmseTest)

    println("Recommendations: (MovieId => Rating)")
    println("----------------------------------")
    val recommendationsUser = model.recommendProducts(414, 10)
    recommendationsUser.map(rating => (rating.product, rating.rating)).foreach(println)
    println("----------------------------------")

  }
}

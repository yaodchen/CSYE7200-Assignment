
package controllers

import org.apache.spark.mllib.recommendation.{ALS, Rating}
import org.apache.spark.sql.types.{DoubleType, IntegerType, StructField, StructType}
import org.apache.spark.sql.{Row, SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import play.api.data._
import play.api.i18n._
import play.api.mvc._
import javax.inject.Inject
import scala.collection._

//import play.api.i18n.Messages._


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */

case class BasicForm(userid: String)
case class Result(res: String)
// this could be defined somewhere else,
// but I prefer to keep it in the companion object
object BasicForm {
  import play.api.data.Forms._
  import play.api.data.Form
  val form: Form[BasicForm] = Form(
    mapping(
      "userid" -> text
    )(BasicForm.apply)(BasicForm.unapply)
  )
}

//case class GameInfo (Name: String, Platform: String, Year_of_Release: String, Genre: String, Publisher: String, NA_Sales: Double, EU_Sales: Double, JP_Sales: Double, Other_Sales: Double, Global_Sales: Double, Critic_Score: Integer, Critic_Count: Integer, User_Score: String, User_Count: Integer, Developer: String, Rating: String)
//class HomeController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {
//@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with play.api.i18n.I18nSupport {
  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def helper(res: String): String = {


    //
      val spark = SparkSession
        .builder()
        .appName("SparkSQL For Csv")
        .master("local[*]")
        .getOrCreate()


    /**
     * Import the dataset as ratingsDF and movieDF
     */
    val ratingsDF = spark.read.format("csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load("/Users/jchen/Desktop/scala_project/src/main/resources/ratings.csv")


    val movieDF = spark.read.format("csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load("/Users/jchen/Desktop/scala_project/src/main/resources/movies.csv")

    /**
     * Implement Spark.SQL to perform data exploration
     */


    ratingsDF.createOrReplaceTempView("ratings")
    movieDF.createOrReplaceTempView("movies")

    /**
     * Split data into train(75%) and test(25%)
     */
    val splits = ratingsDF.randomSplit(Array(0.75, 0.25), seed = 12345L)
    val (trainingData, testData) = (splits(0), splits(1))

    // Train dataset preparation
    val ratingsRDD = trainingData.rdd.map(row => {
      val userId = row.getInt(0)
      val movieId = row.getInt(1)
      val ratings = row.getDouble(2)
      Rating(userId, movieId, ratings)
    })

    // Test dataset preparation
    val testRDD = testData.rdd.map(row => {
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
      .setRank(rank).setSeed(seed)
      .setImplicitPrefs(implicitPrefs)
      .run(ratingsRDD)

    val recommendationsUser = model.recommendProducts(res.toInt, 10)
    val recommendationsRes = recommendationsUser.map(rating => (rating.product, rating.rating))

    // movie id -> title
    val arraylist: Array[(Int, Double)] = recommendationsRes;

    val schema = StructType(
      StructField("movieId", IntegerType, false) ::
        StructField("ratings", DoubleType, false) :: Nil)
    val rdd = spark.sparkContext.parallelize(arraylist).map(x => Row(x._1, x._2.asInstanceOf[Number].doubleValue()))

    val resDF = spark.createDataFrame(rdd, schema)
    resDF.createOrReplaceTempView("resDF")
    val MovieNameRes = spark.sql(
      """
        |SELECT
        |       movies.title
        |FROM resDF JOIN movies
        |ON movies.movieId = resDF.movieId
    """.stripMargin)

    val movie_string = "For user "+res+" , we have recommended 10 new movies for you. "+"\n"+MovieNameRes.select("title").rdd.map(r => r(0)).collect.mkString("\n")
    movie_string

  }


  def analysisPost() = Action { implicit request =>
    val formData: BasicForm = BasicForm.form.bindFromRequest.get // Careful: BasicForm.form.bindFromRequest returns an Option
    val res = helper(formData.userid)

    Ok(res) // just returning the data because it's an example :)
  }
  def recommendation() = Action { implicit request =>
    Ok(views.html.recommendation(BasicForm.form))
  }

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index(BasicForm.form))
  }


}
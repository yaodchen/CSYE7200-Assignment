## Movie Recommendation System

## What is the purpose of this project? 
Everyone loves movies irrespective of age, gender, race, color, or geographical location. We all in a way are connected to each other through this amazing medium. Yet what's most interesting is the fact that how unique our choices and combinations are in terms of movie preferences. Some people like genre-specific movies be it a thriller, romance, or sci-fi, while others focus on lead actors and directors. When we take all that into account, it’s astoundingly difficult to generalize a movie and say that everyone would like it. But with all that said, it is still seen that similar movies are liked by a specific part of the society.

So here’s where we as data scientists come into play and extract the juice out of all the behavioral patterns of not only the audience but also from the movies themselves.

A recommendation system is a subclass of information filtering systems that helps predict the rating or preference based on the likes(ratings) given by users to an item. In recent years, recommendation systems have become increasingly popular. In short, a recommender system tries to predict potential items a user might be interested in based on history for other users.

## Use Cases:
User input: In this case, we assume the input will be one user id that already exists in the database. 
System: The algorithm will return 10 movies recommended by the movie database to this specific user. This user has not seen these movies before. The rating is from high to low.

UI will take user id as input and return the recommendation.

There are a couple of ways to develop recommendation engines that results in a list of recommendations, for eg., collaborative and content-based filtering.

## Collaborative Filtering Approach
Using collaborative filtering approaches, an recommendation system can be built based on a user's past behavior where numerical ratings are given on purchased items.

## Model-Based Collaborative Filtering
In the model-based collaborative filtering technique, users and products are described by a small set of factors, also called latent factors (LFs). The LFs are then used to predict the missing entries. The Alternating Least Squares (ALS) algorithm is used to learn these LFs. From a computational perspective, model-based collaborative filtering is commonly used in many companies such as Netflix for real-time movie recommendations. 

We tried to build a model-based movie recommendation engine with Spark that recommends movies for new users. 
## DataSource
We will use the movie lens dataset for the project. 27,000,000 ratings and 1,100,000 tag applications applied to 58,000 movies by 280,000 users
Includes tag genome data with 14 million relevance scores across 1,100 tags. 


To make a preference prediction for any user, collaborative filtering uses a preference by other users of similar interests and predicts movies of your interests, that are unknown to you. Spark MLlib uses Alternate Least Squares (ALS) to make a recommendation.

## Technology Used
We used Scala and SQL for reading, parsing, data exploration and data modelling. 
For the UI, we had used Play Framework. 

## Conclusion: 
Recommendation Systems are the most popular type of machine learning applications that are used in all sectors. They are an improvement over the traditional classification algorithms as they can take many classes of input and provide similarity ranking based algorithms to provide the user with accurate results. These recommendation systems have evolved over time and have incorporated many advanced machine learning techniques to provide the users with the content that they want.

## Improvements/Next Steps in our project: 
+ Movie genre could be an input to provide more precise result from the recommendation algorithm.
+ New users should be added to the database accordingly.
+ The performance of the preceding model could be increased more. However, so far, there’s no model tuning facility of our knowledge available for the MLlib-based ALS algorithm





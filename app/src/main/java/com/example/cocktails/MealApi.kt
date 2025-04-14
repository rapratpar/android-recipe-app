package com.example.cocktails

import retrofit2.http.GET
import retrofit2.http.Query

interface MealApi {
    @GET("random.php")
    suspend fun getRandomMeal(): MealResponse

    @GET("search.php")
    suspend fun searchMeals(@Query("s") query: String): MealResponse

    @GET("lookup.php")
    suspend fun getMealById(@Query("i") id: String): MealResponse
}
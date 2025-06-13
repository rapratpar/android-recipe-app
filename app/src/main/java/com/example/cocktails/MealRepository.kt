package com.example.cocktails

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MealRepository(
    private val mealDao: MealDao,
    private val api: MealApi
) {

    suspend fun fetchMealFromApi(id: String): Meal? {
        return try {
            val response = api.getMealById(id)
            response.meals.firstOrNull()?.toMeal()
        } catch (e: Exception) {
            Log.e("MealRepository", "API error: ${e.message}")
            null
        }
    }

    suspend fun saveMealOffline(meal: Meal, favorite: Boolean = false, userId: String) {
        val entity = MealEntity(
            id = meal.idMeal,
            name = meal.strMeal,
            thumbnail = meal.strMealThumb,
            instructions = meal.strInstructions,
            isFavorite = favorite,
            isOffline = true,
            userId = userId
        )
        mealDao.insertMeal(entity)
    }

    suspend fun setFavorite(id: String, isFavorite: Boolean, userId: String) {
        mealDao.setFavorite(id, isFavorite, userId)
        if (isFavorite) {
            val meal = mealDao.getMealById(id, userId)
            if (meal != null && !meal.isOffline) {
                mealDao.setOffline(id, true, userId)
            }
        }
    }

    suspend fun setOffline(id: String, isOffline: Boolean, userId: String) {
        mealDao.setOffline(id, isOffline, userId)
    }

    suspend fun getFavoriteMeals(userId: String) = mealDao.getFavoriteMeals(userId)

    suspend fun getOfflineMeals(userId: String) = mealDao.getOfflineMeals(userId)

    suspend fun getMealById(id: String, userId: String) = mealDao.getMealById(id, userId)
}
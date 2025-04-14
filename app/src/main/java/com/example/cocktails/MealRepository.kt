package com.example.cocktails

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MealRepository(
    private val mealDao: MealDao,
    private val api: MealApi // Twoje API z sieci
) {

    suspend fun fetchMealFromApi(id: String): Meal? {
        return try {
            val response = api.getMealById(id)
            response.meals.firstOrNull()
        } catch (e: Exception) {
            Log.e("MealRepository", "API error: ${e.message}")
            null
        }
    }

    suspend fun saveMealOffline(meal: Meal, favorite: Boolean = false) {
        val entity = MealEntity(
            id = meal.idMeal,
            name = meal.strMeal,
            thumbnail = meal.strMealThumb,
            instructions = meal.strInstructions,
            isFavorite = favorite,
            isOffline = true
        )
        mealDao.insertMeal(entity)
    }

    suspend fun setFavorite(id: String, isFavorite: Boolean) {
        mealDao.setFavorite(id, isFavorite)
        if (isFavorite) {
            val meal = mealDao.getMealById(id)
            if (meal != null && !meal.isOffline) {
                mealDao.setOffline(id, true)
            }
        }
    }

    suspend fun setOffline(id: String, isOffline: Boolean) {
        mealDao.setOffline(id, isOffline)
    }

    suspend fun getFavoriteMeals() = mealDao.getFavoriteMeals()

    suspend fun getOfflineMeals() = mealDao.getOfflineMeals()

    suspend fun getMealById(id: String) = mealDao.getMealById(id)
}
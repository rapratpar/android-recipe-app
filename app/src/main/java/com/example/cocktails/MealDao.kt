package com.example.cocktails

import androidx.room.*

@Dao
interface MealDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    @Query("SELECT * FROM meals")
    suspend fun getAllMeals(): List<MealEntity>

    @Query("SELECT * FROM meals WHERE isFavorite = 1")
    suspend fun getFavoriteMeals(): List<MealEntity>

    @Query("SELECT * FROM meals WHERE isOffline = 1")
    suspend fun getOfflineMeals(): List<MealEntity>

    @Query("SELECT * FROM meals WHERE id = :id")
    suspend fun getMealById(id: String): MealEntity?

    @Delete
    suspend fun deleteMeal(meal: MealEntity)

    @Query("UPDATE meals SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE meals SET isOffline = :isOffline WHERE id = :id")
    suspend fun setOffline(id: String, isOffline: Boolean)
}
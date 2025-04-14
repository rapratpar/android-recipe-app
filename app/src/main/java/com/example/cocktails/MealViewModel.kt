package com.example.cocktails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MealViewModel(application: Application) : AndroidViewModel(application) {

    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val meals = _meals.asStateFlow()

    // Room + API setup
    private val database = AppDatabase.getDatabase(application)
    private val mealDao = database.mealDao()
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.themealdb.com/api/json/v1/1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val api = retrofit.create(MealApi::class.java)

    val repository = MealRepository(mealDao, api) // <- Uwaga: udostępniamy repo do UI

    init {
        loadRandomMeals()
    }

    fun loadRandomMeals(count: Int = 10) {
        viewModelScope.launch {
            val randoms = mutableListOf<Meal>()
            repeat(count) {
                try {
                    val response = api.getRandomMeal()
                    response.meals.firstOrNull()?.let {
                        randoms.add(it)
                    }
                } catch (e: Exception) {
                    println("Błąd API: ${e.message}")
                }
            }
            _meals.value = randoms
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            try {
                val response = api.searchMeals(query)
                _meals.value = response.meals ?: emptyList()
            } catch (_: Exception) {
                _meals.value = emptyList()
            }
        }
    }

    fun toggleFavorite(meal: Meal) {
        viewModelScope.launch {
            val existing = repository.getMealById(meal.idMeal)
            val isNowFavorite = !(existing?.isFavorite ?: false)
            repository.setFavorite(meal.idMeal, isNowFavorite)
            if (existing == null) {
                repository.saveMealOffline(meal, favorite = true)
            }
        }
    }

    fun saveOffline(meal: Meal) {
        viewModelScope.launch {
            val existing = repository.getMealById(meal.idMeal)
            if (existing == null) {
                repository.saveMealOffline(meal)
            } else {
                repository.setOffline(meal.idMeal, true)
            }
        }
    }

    fun removeOffline(mealId: String) {
        viewModelScope.launch {
            repository.setOffline(mealId, false)
        }
    }

    fun getFavoriteMeals(onResult: (List<MealEntity>) -> Unit) {
        viewModelScope.launch {
            onResult(repository.getFavoriteMeals())
        }
    }

    fun getOfflineMeals(onResult: (List<MealEntity>) -> Unit) {
        viewModelScope.launch {
            onResult(repository.getOfflineMeals())
        }
    }
}
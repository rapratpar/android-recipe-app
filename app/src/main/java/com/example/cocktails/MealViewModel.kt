package com.example.cocktails

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MealViewModel(application: Application) : AndroidViewModel(application) {

    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val meals = _meals.asStateFlow()

    private val database = AppDatabase.getDatabase(application)
    private val mealDao = database.mealDao()
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.themealdb.com/api/json/v1/1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val api = retrofit.create(MealApi::class.java)

    val repository = MealRepository(mealDao, api)

    val isLoggedIn: Boolean
        get() = FirebaseAuth.getInstance().currentUser != null

    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

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
                        randoms.add(it.toMeal())
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
                _meals.value = response.meals?.map { it.toMeal() } ?: emptyList()
            } catch (_: Exception) {
                _meals.value = emptyList()
            }
        }
    }

    fun toggleFavorite(meal: Meal) {
        if (!isLoggedIn) {
            Log.d("MealViewModel", "Użytkownik niezalogowany – nie można dodać do ulubionych")
            return
        }

        viewModelScope.launch {
            val uid = getCurrentUserId()
            if (uid == null) return@launch
            val existing = repository.getMealById(meal.idMeal, uid)

            if (existing == null) {
                repository.saveMealOffline(meal, favorite = true, userId = uid)
            } else {
                val isNowFavorite = !existing.isFavorite
                repository.setFavorite(meal.idMeal, isNowFavorite, uid)
            }
        }
    }

    fun saveOffline(meal: Meal) {
        if (!isLoggedIn) {
            Log.d("MealViewModel", "Użytkownik niezalogowany – nie można zapisać offline")
            return
        }

        viewModelScope.launch {
            val uid = getCurrentUserId()
            if (uid != null) {
                val existing = repository.getMealById(meal.idMeal, uid)
                if (existing == null) {
                    repository.saveMealOffline(meal, userId = uid)
                } else {
                    repository.setOffline(meal.idMeal, true, uid)
                }
            }
        }
    }

    fun removeOffline(mealId: String) {
        viewModelScope.launch {
            val uid = getCurrentUserId()
            if (uid != null) {
                repository.setOffline(mealId, false, uid)
            }
        }
    }

    fun getFavoriteMeals(onResult: (List<MealEntity>) -> Unit) {
        viewModelScope.launch {
            getCurrentUserId()?.let { repository.getFavoriteMeals(it) }?.let { onResult(it) }
        }
    }

    fun getOfflineMeals(onResult: (List<MealEntity>) -> Unit) {
        viewModelScope.launch {
            getCurrentUserId()?.let { repository.getOfflineMeals(it) }?.let { onResult(it) }
        }
    }
}

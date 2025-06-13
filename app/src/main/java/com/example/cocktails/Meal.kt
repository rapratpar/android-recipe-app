package com.example.cocktails

data class Meal(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String,
    val strInstructions: String,
    val ingredients: List<Pair<String, String>> = emptyList()
)
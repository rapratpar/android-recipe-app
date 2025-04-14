package com.example.cocktails

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch

@Composable
fun RecipeDetailScreen(
    mealId: String,
    viewModel: MealViewModel
) {
    var meal by remember { mutableStateOf<Meal?>(null) }
    var isFavorite by remember { mutableStateOf(false) }
    var isOffline by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(mealId) {
        val local = viewModel.repository.getMealById(mealId)
        if (local != null) {
            meal = Meal(
                idMeal = local.id,
                strMeal = local.name,
                strMealThumb = local.thumbnail,
                strInstructions = local.instructions
            )
            isFavorite = local.isFavorite
            isOffline = local.isOffline
        } else {
            meal = viewModel.repository.fetchMealFromApi(mealId)
        }
    }

    meal?.let {
        Column(modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()) {
            Text(it.strMeal, style = MaterialTheme.typography.h4)
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = rememberAsyncImagePainter(it.strMealThumb),
                contentDescription = it.strMeal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(it.strInstructions ?: "Brak instrukcji", style = MaterialTheme.typography.body1)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = {
                    coroutineScope.launch {
                        viewModel.toggleFavorite(it)
                        isFavorite = !isFavorite
                        isOffline = true
                    }
                }) {
                    Text(if (isFavorite) "Usuń z ulubionych" else "Dodaj do ulubionych")
                }

                Button(onClick = {
                    coroutineScope.launch {
                        if (isOffline) {
                            viewModel.removeOffline(it.idMeal)
                        } else {
                            viewModel.saveOffline(it)
                        }
                        isOffline = !isOffline
                    }
                }) {
                    Text(if (isOffline) "Usuń offline" else "Zapisz offline")
                }
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
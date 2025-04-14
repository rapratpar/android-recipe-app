package com.example.cocktails

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument

@Composable
fun App(viewModel: MealViewModel) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "main?message={message}") {
        // 🔹 Main z obsługą parametru message
        composable(
            route = "main?message={message}",
            arguments = listOf(
                navArgument("message") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val message = backStackEntry.arguments?.getString("message")
            MainScreen(navController,  mealViewModel = viewModel, message = message)
        }

        // 🔹 Szczegóły przepisu
        composable("details/{mealId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("mealId") ?: ""
            RecipeDetailScreen(mealId = id, viewModel = viewModel)
        }

        // 🔹 Ekran logowania
        composable("auth") {
            AuthScreen(navController)
        }
    }
}
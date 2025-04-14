package com.example.cocktails

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen(
    navController: NavController,
    mealViewModel: MealViewModel,
    authViewModel: AuthViewModel = viewModel(),
    message: String? = null
) {
    val meals by mealViewModel.meals.collectAsState()
    var search by remember { mutableStateOf("") }
    var selectedScreen by remember { mutableStateOf("Wszystkie") }
    var favoriteMeals by remember { mutableStateOf(emptyList<MealEntity>()) }
    var offlineMeals by remember { mutableStateOf(emptyList<MealEntity>()) }
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.takeIf { it.isNotBlank() }?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(Unit) {
        mealViewModel.getFavoriteMeals { favoriteMeals = it }
        mealViewModel.getOfflineMeals { offlineMeals = it }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = search,
                    onValueChange = {
                        search = it
                        if (it.isEmpty()) mealViewModel.loadRandomMeals()
                        else mealViewModel.search(it)
                    },
                    label = { Text("Szukaj przepisu") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                var expanded by remember { mutableStateOf(false) }
                Box {
                    Button(onClick = { expanded = true }) {
                        Text(selectedScreen)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(onClick = {
                            selectedScreen = "Wszystkie"
                            expanded = false
                        }) { Text("Wszystkie") }
                        DropdownMenuItem(onClick = {
                            selectedScreen = "Ulubione"
                            expanded = false
                        }) { Text("Ulubione") }
                        DropdownMenuItem(onClick = {
                            selectedScreen = "Zapisane"
                            expanded = false
                        }) { Text("Zapisane") }
                    }
                }
            }

            if (isLoggedIn) {
                Button(
                    onClick = {
                        authViewModel.logout {
                            navController.navigate("auth") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Wyloguj się")
                }
            } else {
                Button(
                    onClick = { navController.navigate("auth") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Zaloguj się")
                }
            }

            when (selectedScreen) {
                "Wszystkie" -> MealList(meals.map {
                    MealEntity(
                        id = it.idMeal,
                        name = it.strMeal,
                        thumbnail = it.strMealThumb,
                        instructions = it.strInstructions,
                        isFavorite = favoriteMeals.any { fav -> fav.id == it.idMeal },
                        isOffline = offlineMeals.any { off -> off.id == it.idMeal }
                    )
                }, navController)

                "Ulubione" -> MealList(favoriteMeals, navController)

                "Zapisane" -> MealList(offlineMeals, navController)
            }
        }
    }
}

@Composable
fun MealList(meals: List<MealEntity>, navController: NavController) {
    LazyColumn {
        items(meals) { meal ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable {
                        navController.navigate("details/${meal.id}")
                    },
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Image(
                            painter = rememberAsyncImagePainter(meal.thumbnail),
                            contentDescription = meal.name,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(meal.name, style = MaterialTheme.typography.h6)
                    }
                    Row {
                        Icon(
                            imageVector = if (meal.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Ulubione",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Icon(
                            imageVector = if (meal.isOffline) Icons.Filled.Save else Icons.Outlined.Save,
                            contentDescription = "Zapisane"
                        )
                    }
                }
            }
        }
    }
}
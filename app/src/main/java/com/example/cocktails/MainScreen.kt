package com.example.cocktails

import android.content.Context
import android.net.ConnectivityManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val context = LocalContext.current
    val isOnline = remember { isInternetAvailable(context) }

    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    var lastClickTime by remember { mutableStateOf(0L) }
    val drawerEnabled = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        drawerEnabled.value = true
    }

    LaunchedEffect(message) {
        message?.takeIf { it.isNotBlank() }?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(Unit) {
        try {
            mealViewModel.getFavoriteMeals { favoriteMeals = it }
            mealViewModel.getOfflineMeals { offlineMeals = it }
        } catch (e: Exception) {
            snackbarHostState.showSnackbar("Błąd ładowania danych")
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(screenWidth * 0.4f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.cookit_logo),
                            contentDescription = "Logo aplikacji",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "CookIt",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Divider()

                    DrawerItem("Wszystkie", Icons.Default.List) {
                        selectedScreen = "Wszystkie"
                        coroutineScope.launch { drawerState.close() }
                    }
                    DrawerItem("Ulubione", Icons.Default.Favorite) {
                        navController.navigate("favorites")
                        coroutineScope.launch { drawerState.close() }
                    }
                    DrawerItem("Zapisane", Icons.Default.Save) {
                        navController.navigate("offline")
                        coroutineScope.launch { drawerState.close() }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isLoggedIn) {
                        DrawerItem("Wyloguj się", Icons.Default.Logout) {
                            authViewModel.logout {
                                navController.navigate("login") {
                                    popUpTo("main?message={message}") { inclusive = true }
                                }
                            }
                        }
                    } else {
                        DrawerItem("Zaloguj się", Icons.Default.Person) {
                            navController.navigate("login")
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopBar(
                    title = "Menu",
                    isMainScreen = true,
                    onNavigationClick = {
                        val now = System.currentTimeMillis()
                        if (now - lastClickTime > 500 && drawerEnabled.value) {
                            lastClickTime = now
                            coroutineScope.launch {
                                if (!drawerState.isOpen) {
                                    drawerState.open()
                                }
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                OutlinedTextField(
                    value = search,
                    onValueChange = {
                        search = it
                        if (it.isEmpty()) mealViewModel.loadRandomMeals()
                        else mealViewModel.search(it)
                    },
                    label = { Text("Szukaj przepisu") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                when (selectedScreen) {
                    "Wszystkie" -> {
                        val safeMeals = meals ?: emptyList()
                        val displayMeals = if (isOnline) {
                            safeMeals.map {
                                MealEntity(
                                    id = it.idMeal,
                                    name = it.strMeal,
                                    thumbnail = it.strMealThumb,
                                    instructions = it.strInstructions,
                                    isFavorite = favoriteMeals.any { fav -> fav.id == it.idMeal },
                                    isOffline = offlineMeals.any { off -> off.id == it.idMeal },
                                    userId = userId
                                )
                            }
                        } else offlineMeals

                        MealList(displayMeals, navController)
                    }
                    "Ulubione" -> MealList(favoriteMeals, navController)
                    "Zapisane" -> MealList(offlineMeals, navController)
                }
            }
        }
    }
}

@Composable
fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
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
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = meal.thumbnail,
                        contentDescription = meal.name,
                        placeholder = painterResource(R.drawable.cookit_logo),
                        error = painterResource(R.drawable.cookit_logo),
                        modifier = Modifier
                            .size(80.dp)
                            .padding(end = 8.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = meal.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 2
                        )
                    }

                    Row(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .wrapContentWidth()
                            .align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = if (meal.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Ulubione",
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(24.dp)
                        )
                        Icon(
                            imageVector = if (meal.isOffline) Icons.Filled.Save else Icons.Outlined.Save,
                            contentDescription = "Zapisane",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetworkInfo
    return activeNetwork != null && activeNetwork.isConnected
}
@Composable
fun FavoriteMealsScreen(navController: NavController, mealViewModel: MealViewModel) {
    val favoriteMealsState = remember { mutableStateOf(emptyList<MealEntity>()) }
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var canNavigateBack by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (isLoggedIn) {
            try {
                mealViewModel.getFavoriteMeals { meals ->
                    favoriteMealsState.value = meals
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Błąd ładowania ulubionych")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopBar(title = "Ulubione", isMainScreen = false, onNavigationClick = {
                if (canNavigateBack && navController.previousBackStackEntry != null) {
                    canNavigateBack = false
                    navController.popBackStack()
                    coroutineScope.launch {
                        delay(500)
                        canNavigateBack = true
                    }
                }
            })
        }
    )  { padding ->
        if (!isLoggedIn) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Proszę się zalogować",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                )
            }
        } else {
            Box(modifier = Modifier.padding(padding)) {
                MealList(favoriteMealsState.value, navController)
            }
        }
    }
}

@Composable
fun OfflineMealsScreen(navController: NavController, mealViewModel: MealViewModel) {
    val offlineMealsState = remember { mutableStateOf(emptyList<MealEntity>()) }
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var canNavigateBack by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (isLoggedIn) {
            try {
                mealViewModel.getOfflineMeals { meals ->
                    offlineMealsState.value = meals
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Błąd ładowania zapisanych")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopBar(title = "Zapisane", isMainScreen = false, onNavigationClick = {
                if (canNavigateBack && navController.previousBackStackEntry != null) {
                    canNavigateBack = false
                    navController.popBackStack()
                    coroutineScope.launch {
                        delay(500)
                        canNavigateBack = true
                    }
                }
            })
        }
    ) { padding ->
        if (!isLoggedIn) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Proszę się zalogować",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                )
            }
        } else {
            Box(modifier = Modifier.padding(padding)) {
                MealList(offlineMealsState.value, navController)
            }
        }
    }
}
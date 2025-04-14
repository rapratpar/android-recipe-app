package com.example.cocktails

import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AuthScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val message = viewModel.loginMessage.value
    val showSnackbar = viewModel.showSnackbar

    // Pokazywanie Snackbara przy zmianie komunikatu
    LaunchedEffect(showSnackbar.value) {
        if (showSnackbar.value && message != null) {
            snackbarHostState.showSnackbar(message)
            showSnackbar.value = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLogin by remember { mutableStateOf(true) }

        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(text = if (isLogin) "Logowanie" else "Rejestracja")

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Hasło") },
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                if (isLogin) {
                    viewModel.login(email, password) {
                        navController.navigate("main?message=Udało się zalogować!") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                } else {
                    viewModel.register(email, password) {
                        navController.navigate("main?message=Rejestracja zakończona sukcesem!") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                }
            }) {
                Text(text = if (isLogin) "Zaloguj się" else "Zarejestruj się")
            }

            TextButton(onClick = { isLogin = !isLogin }) {
                Text(if (isLogin) "Nie masz konta? Zarejestruj się" else "Masz już konto? Zaloguj się")
            }
        }
    }
}
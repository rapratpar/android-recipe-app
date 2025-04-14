package com.example.cocktails

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.*

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val loginMessage = mutableStateOf<String?>(null)
    val showSnackbar = mutableStateOf(false)

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loginMessage.value = "Udało się zalogować!"
                    showSnackbar.value = true
                    onSuccess()
                } else {
                    loginMessage.value = "Błąd logowania: ${task.exception?.localizedMessage ?: "Nieznany błąd"}"
                    showSnackbar.value = true
                }
            }
    }

    fun register(email: String, password: String, onSuccess: () -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loginMessage.value = "Rejestracja zakończona sukcesem!"
                    showSnackbar.value = true
                    onSuccess()
                } else {
                    loginMessage.value = "Błąd rejestracji: ${task.exception?.localizedMessage ?: "Nieznany błąd"}"
                    showSnackbar.value = true
                }
            }
    }

    fun logout(onLoggedOut: () -> Unit) {
        FirebaseAuth.getInstance().signOut()
        onLoggedOut()
    }
}

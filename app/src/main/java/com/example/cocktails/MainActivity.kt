package com.example.cocktails

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    // <- tutaj tworzysz ViewModel z factory!
    private val viewModel: MealViewModel by viewModels {
        MealViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        Log.d("Start", "poszlo!")

        setContent {
                Surface(color = MaterialTheme.colorScheme.background) {
                    App(viewModel) // <- przekazujesz ViewModel do całej aplikacj
            }
        }
    }
}
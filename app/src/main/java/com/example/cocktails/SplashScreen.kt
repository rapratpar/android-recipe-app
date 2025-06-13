package com.example.cocktails

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cocktails.ui.theme.BlackBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    var animate by remember { mutableStateOf(false) }

    val scale = animateFloatAsState(
        targetValue = if (animate) 60f else 1f,
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        ),
        label = "scaleAnimation"
    )

    LaunchedEffect(true) {
        animate = true
        delay(800)
        navController.navigate("main") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.cookit_logo),
            contentDescription = "Logo",
            modifier = Modifier
                .scale(scale.value)
                .size(100.dp)
                .clip(CircleShape)
        )
    }
}

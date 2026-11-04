package com.expensetracker.presentation.ui.screen.splash

import androidx.compose.animation.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.presentation.theme.AppTheme
import com.expensetracker.presentation.theme.Green600
import com.expensetracker.presentation.viewmodel.SplashViewModel

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigateToOnboarding: () -> Unit
) {
    val isFirstLaunch by viewModel.isFirstLaunch.collectAsState()
    val shouldNavigate by viewModel.shouldNavigate.collectAsState()

    val logoScale by animateFloatAsState(
        targetValue = if (shouldNavigate) 0.8f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f)
    )

    val progressAlpha by animateFloatAsState(
        targetValue = if (shouldNavigate) 0f else 1f,
        animationSpec = tween(500)
    )

    LaunchedEffect(shouldNavigate) {
        if (shouldNavigate) {
            onNavigateToOnboarding()
        }
    }

    AppTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer { scaleX = logoScale; scaleY = logoScale }
                        .size(120.dp)
                ) {
                    // App logo/icon placeholder
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(120.dp)) {
                        drawCircle(
                            color = Green600,
                            center = androidx.compose.ui.geometry.Offset(60.dp.toPx(), 60.dp.toPx()),
                            radius = 60.dp.toPx()
                        )
                        drawText(
                            text = "💰",
                            fontSize = 60.sp,
                            color = Color.White,
                            topLeft = androidx.compose.ui.geometry.Offset(30.dp.toPx(), 80.dp.toPx())
                        )
                    }
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Expense Tracker",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1C)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Smart money management",
                    fontSize = 16.sp,
                    color = Color(0xFF757575)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(48.dp))
                CircularProgressIndicator(
                    modifier = Modifier
                        .graphicsLayer { alpha = progressAlpha }
                        .width(48.dp)
                        .height(48.dp),
                    color = Green600,
                    trackColor = Color(0xFFEEEEEE),
                    strokeWidth = 4.dp
                )
            }
        }
    }
}

@Composable
fun SplashPreview() {
    AppTheme {
        SplashScreen(
            viewModel = SplashViewModel(),
            onNavigateToOnboarding = {}
        )
    }
}
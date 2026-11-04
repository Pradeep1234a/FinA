package com.expensetracker.presentation.ui.component

import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoadingState(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        CircularProgressIndicator(color = Color(0xFF1B5E20), modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        androidx.compose.material3.Text(text = message, fontSize = 16.sp, color = Color(0xFF757575))
    }
}

@Composable
fun SkeletonLoader(modifier: Modifier = Modifier) {
    val alpha by remember { mutableStateOf(0.3f) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
            .background(Color(0xFFE0E0E0).copy(alpha = alpha))
    )
}

@Composable
fun TransactionSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            SkeletonLoader(modifier = Modifier.weight(1f).height(20.dp).padding(end = 16.dp))
            SkeletonLoader(modifier = Modifier.width(80.dp).height(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonLoader(modifier = Modifier.fillMaxWidth().height(14.dp))
        Spacer(modifier = Modifier.height(4.dp))
        SkeletonLoader(modifier = Modifier.width(100.dp).height(12.dp))
    }
}
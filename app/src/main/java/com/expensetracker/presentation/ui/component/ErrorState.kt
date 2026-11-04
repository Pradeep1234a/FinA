package com.expensetracker.presentation.ui.component

import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ErrorState(
    title: String = "Something went wrong",
    message: String = "Please try again later",
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFB71C1C)
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.W600,
            color = Color(0xFF1C1C1C),
            textAlign = androidx.compose.ui.text.TextAlign.Center
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 16.sp,
            color = Color(0xFF757575),
            textAlign = androidx.compose.ui.text.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        if (onRetry != null) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Text(text = "Try Again", fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.W600, color = Color.White)
                }
            }
        }
    }
}
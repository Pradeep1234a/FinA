package com.expensetracker.presentation.ui.component

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CircularBudgetProgress(
    progress: Float,
    modifier: Modifier = Modifier.size(80.dp),
    strokeWidth: androidx.compose.ui.unit.Dp = 8.dp,
    trackColor: Color = Color(0xFFEEEEEE),
    progressColor: Color = Color(0xFF1B5E20),
    content: @Composable () -> Unit = {}
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier = Modifier.size(80.dp),
            strokeWidth = strokeWidth,
            trackColor = trackColor,
            color = progressColor
        )
        content()
    }
}

@Composable
fun LinearBudgetProgress(
    progress: Float,
    modifier: Modifier = Modifier.fillMaxWidth().height(8.dp),
    trackColor: Color = Color(0xFFEEEEEE),
    progressColor: Color = Color(0xFF1B5E20)
) {
    LinearProgressIndicator(
        progress = progress.coerceIn(0f, 1f),
        modifier = modifier,
        trackColor = trackColor,
        color = progressColor
    )
}

@Composable
fun BudgetProgressCard(
    title: String,
    spent: Double,
    budget: Double,
    period: String,
    modifier: Modifier = Modifier
) {
    val progress = if (budget > 0) (spent / budget).toFloat() else 0f
    val isOverBudget = spent > budget
    val progressColor = if (isOverBudget) Color(0xFFB71C1C) else Color(0xFF1B5E20)

    androidx.compose.material3.Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text(text = title, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.W600, color = Color(0xFF1C1C1C))
                Text(text = period, fontSize = 14.sp, color = Color(0xFF757575))
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            LinearBudgetProgress(progress = progress, progressColor = progressColor)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text(text = "Spent: $${String.format("%.2f", spent)}", fontSize = 14.sp, color = Color(0xFF1C1C1C))
                Text(text = "Budget: $${String.format("%.2f", budget)}", fontSize = 14.sp, color = Color(0xFF757575))
            }
        }
    }
}
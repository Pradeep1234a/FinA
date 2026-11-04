package com.expensetracker.presentation.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rohitvds.compose.charts.PieChart
import com.rohitvds.compose.charts.BarChart
import com.rohitvds.compose.charts.LineChart
import com.rohitvds.compose.charts.data.PieEntry
import com.rohitvds.compose.charts.data.BarEntry
import com.rohitvds.compose.charts.data.LineEntry

@Composable
fun ExpensePieChart(
    data: List<Pair<String, Double>>,
    colors: List<Color> = defaultColors,
    modifier: Modifier = Modifier.fillMaxSize().height(300.dp)
) {
    val entries = data.mapIndexed { index, (label, value) ->
        PieEntry(value = value.toFloat(), label = label, color = colors[index % colors.size])
    }

    Card(
        modifier = modifier,
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            PieChart(
                entries = entries,
                modifier = Modifier.fillMaxSize(),
                centerText = "Expenses",
                showLabels = true,
                holeRadius = 60f
            )
        }
    }
}

@Composable
fun MonthlyBarChart(
    data: List<Pair<String, Double>>,
    color: Color = Color(0xFF1B5E20),
    modifier: Modifier = Modifier.fillMaxSize().height(300.dp)
) {
    val entries = data.mapIndexed { index, (label, value) ->
        BarEntry(value = value.toFloat(), label = label, color = color)
    }

    Card(
        modifier = modifier,
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            BarChart(
                entries = entries,
                modifier = Modifier.fillMaxSize(),
                showLabels = true,
                barWidth = 0.6f
            )
        }
    }
}

@Composable
fun TrendLineChart(
    data: List<Pair<String, Double>>,
    color: Color = Color(0xFF1B5E20),
    modifier: Modifier = Modifier.fillMaxSize().height(300.dp)
) {
    val entries = data.mapIndexed { index, (label, value) ->
        LineEntry(x = index.toFloat(), y = value.toFloat())
    }

    Card(
        modifier = modifier,
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            LineChart(
                entries = entries,
                modifier = Modifier.fillMaxSize(),
                color = color,
                strokeWidth = 3f,
                showLabels = true,
                cubic = true
            )
        }
    }
}

@Composable
fun CategoryBarChart(
    data: List<Pair<String, Double>>,
    colors: List<Color> = defaultColors,
    modifier: Modifier = Modifier.fillMaxSize().height(300.dp)
) {
    val entries = data.mapIndexed { index, (label, value) ->
        BarEntry(value = value.toFloat(), label = label, color = colors[index % colors.size])
    }

    Card(
        modifier = modifier,
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            BarChart(
                entries = entries,
                modifier = Modifier.fillMaxSize(),
                showLabels = true,
                barWidth = 0.5f
            )
        }
    }
}

private val defaultColors = listOf(
    Color(0xFF1B5E20), Color(0xFF1565C0), Color(0xFFBF360C),
    Color(0xFF6A1B9A), Color(0xFF00695C), Color(0xFFE65100),
    Color(0xFFC62828), Color(0xFF37474F)
)
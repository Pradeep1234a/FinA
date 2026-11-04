package com.expensetracker.presentation.ui.component

import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun DatePickerButton(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    androidx.compose.material3.TextButton(
        onClick = { showPicker = true },
        modifier = modifier
    ) {
        Text(text = selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")), fontSize = 16.sp, color = Color(0xFF1C1C1C))
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = { androidx.compose.material3.TextButton(onClick = { showPicker = false }) { Text("OK") } },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { showPicker = false }) { Text("Cancel") } },
            initialDate = selectedDate,
            onDateSelected = onDateSelected
        )
    }
}

@Composable
fun DateRangePicker(
    startDate: LocalDate,
    endDate: LocalDate,
    onStartDateSelected: (LocalDate) -> Unit,
    onEndDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        DatePickerButton(selectedDate = startDate, onDateSelected = onStartDateSelected, modifier = Modifier.weight(1f).padding(end = 8.dp))
        Text(text = "to", fontSize = 16.sp, color = Color(0xFF757575), modifier = Modifier.padding(horizontal = 8.dp))
        DatePickerButton(selectedDate = endDate, onDateSelected = onEndDateSelected, modifier = Modifier.weight(1f).padding(start = 8.dp))
    }
}
package com.expensetracker.presentation.ui.component

import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.model.TransactionType

@Composable
fun TransactionCard(
    transaction: Transaction,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.Income
    val amountColor = if (isIncome) Color(0xFF1B5E20) else Color(0xFFB71C1C)
    val amountPrefix = if (isIncome) "+" else "-"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        onClick = onClick,
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(1f).padding(end = 16.dp)
            ) {
                Text(
                    text = transaction.categoryName,
                    fontSize = 16.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.W600,
                    color = Color(0xFF1C1C1C)
                )
                if (transaction.note.isNotBlank()) {
                    Text(
                        text = transaction.note,
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = transaction.date.format("MMM dd, yyyy"),
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
            androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$amountPrefix${transaction.amount.formatAmount()}",
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = amountColor
                )
            }
        }
    }
}

private fun Double.formatAmount(): String = String.format("%.2f", this)
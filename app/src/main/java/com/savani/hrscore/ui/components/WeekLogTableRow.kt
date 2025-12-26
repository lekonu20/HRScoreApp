package com.savani.hrscore.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.savani.hrscore.network.LogWeekRow

@Composable
fun WeekLogDialog(
    week: Int,
    rows: List<LogWeekRow>,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(onClick = onClose) { Text("Đóng") }
        },
        title = { Text("Chi tiết Tuần $week") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                WeekLogTableHeader()

                if (rows.isEmpty()) {
                    Text(
                        "Không có dữ liệu",
                        modifier = Modifier.padding(12.dp),
                        color = Color.Gray
                    )
                    return@AlertDialog
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                ) {
                    items(rows) { item ->
                        WeekLogTableRow(item = item)
                    }
                }
            }
        }
    )
}

@Composable
private fun WeekLogTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F1F1))
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text("Ngày", modifier = Modifier.weight(0.25f), fontWeight = FontWeight.Bold)
        Text("Lỗi", modifier = Modifier.weight(0.55f), fontWeight = FontWeight.Bold)
        Text("SL", modifier = Modifier.weight(0.20f), fontWeight = FontWeight.Bold)
    }
    Divider()
}

@Composable
private fun WeekLogTableRow(item: LogWeekRow) {
    val qtyColor = when {
        item.count >= 3 -> Color.Red
        item.count == 2 -> Color(0xFFFF9800)
        else -> Color.Gray
    }

    val ddmm = if (item.date.length >= 10) {
        item.date.substring(8, 10) + "/" + item.date.substring(5, 7)
    } else item.date

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(ddmm, modifier = Modifier.weight(0.25f), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(item.code, modifier = Modifier.weight(0.55f), style = MaterialTheme.typography.bodyMedium)
        Text(
            item.count.toString(),
            modifier = Modifier.weight(0.20f),
            color = qtyColor,
            fontWeight = FontWeight.Bold
        )
    }
    Divider()
}

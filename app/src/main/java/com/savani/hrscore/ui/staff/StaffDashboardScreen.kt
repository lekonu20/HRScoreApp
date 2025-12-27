package com.savani.hrscore.ui.staff

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.savani.hrscore.model.StaffScoreRow
import com.savani.hrscore.viewmodel.StaffDashboardViewModel

@Composable
fun StaffDashboardScreen(
    navController: NavController,
    vm: StaffDashboardViewModel = viewModel()
) {
    var month by remember { mutableStateOf(currentMonthYYYYMM()) }
    val ui by vm.uiState.collectAsState()

    LaunchedEffect(month) {
        vm.load(month)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        // ===== HEADER =====
        Text("Tổng điểm nhân sự", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = month,
                onValueChange = { month = it },
                label = { Text("Tháng (yyyy-MM)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { vm.load(month) }, enabled = !ui.loading) {
                Text(if (ui.loading) "Đang tải..." else "Tải")
            }
        }

        Spacer(Modifier.height(12.dp))

        // ===== BODY: 1 LazyColumn để scroll tất cả =====
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {
                Text("Danh sách nhân sự", fontWeight = FontWeight.Bold)
            }

            if (ui.loading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) { CircularProgressIndicator() }
                }
            } else if (ui.staffScores.isEmpty()) {
                item { Text("Chưa có dữ liệu", color = Color.Gray) }
            } else {
                items(ui.staffScores) { staff ->
                    StaffScoreRowItem(
                        staff = staff,
                        onClick = {
                            navController.navigate("staff_detail/${staff.staffId}/${staff.name}")
                        }
                    )
                }
            }

            // ===== TOP LỖI TUẦN =====
            item {
                Spacer(Modifier.height(8.dp))
                TopCodesSection(
                    title = "🔥 Top lỗi tuần",
                    items = ui.topWeekCodes
                )
            }

            // ===== TOP LỖI THÁNG =====
            item {
                TopCodesSection(
                    title = "📊 Top lỗi tháng",
                    items = ui.topMonthCodes
                )
            }

            // ===== ERROR =====
            ui.error?.let { err ->
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Lỗi: $err", color = Color.Red)
                }
            }
        }
    }
}

@Composable
private fun StaffScoreRowItem(
    staff: StaffScoreRow,
    onClick: () -> Unit
) {
    val scoreInt = staff.score.toIntOrNull() ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(staff.name, fontWeight = FontWeight.Medium)
            Text(
                staff.score,
                fontWeight = FontWeight.Bold,
                color = if (scoreInt < 90) Color.Red else Color(0xFF2E7D32)
            )
        }
    }
}

@Composable
private fun TopCodesSection(
    title: String,
    items: List<com.savani.hrscore.network.CodeCount>
) {
    Text(title, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(6.dp))

    // Debug nhìn cho chắc dữ liệu đã về chưa (có thể xóa sau)
    Text("DEBUG size=${items.size}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
    Spacer(Modifier.height(6.dp))

    if (items.isEmpty()) {
        Text("Chưa có dữ liệu", color = Color.Gray)
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.take(10).forEachIndexed { idx, it ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${idx + 1}. ${it.code}")
                    Text("${it.count} lần", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun currentMonthYYYYMM(): String {
    val cal = java.util.Calendar.getInstance()
    val y = cal.get(java.util.Calendar.YEAR)
    val m = cal.get(java.util.Calendar.MONTH) + 1
    return "%04d-%02d".format(y, m)
}

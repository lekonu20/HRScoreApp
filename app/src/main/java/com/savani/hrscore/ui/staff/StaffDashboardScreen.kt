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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.savani.hrscore.model.CodeItem
import com.savani.hrscore.model.StaffScoreRow
import com.savani.hrscore.network.RetrofitClient
import com.savani.hrscore.viewmodel.StaffDashboardViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboardScreen(
    navController: NavController,
    vm: StaffDashboardViewModel = viewModel()
) {
    // ✅ list tháng để chọn (12 tháng gần nhất)
    val monthOptions = remember { buildRecentMonthsOptions(12) }

    var month by remember { mutableStateOf(currentMonthYYYYMM()) }
    var monthExpanded by remember { mutableStateOf(false) }

    val ui by vm.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // ✅ Load Codes để đổi L7/D3 -> tên lỗi
    var codes by remember { mutableStateOf<List<CodeItem>>(emptyList()) }
    var codesLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val res = RetrofitClient.api.getCodes()
                if (res.ok) {
                    codes = res.data
                    codesLoaded = true
                }
            } catch (_: Exception) { }
        }
    }

    val codeNameMap = remember(codes) {
        codes.associate { it.code to ((it.desc ?: "").trim()) }
    }

    // ✅ Tự load mỗi khi đổi tháng
    LaunchedEffect(month) {
        vm.load(month)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text("Tổng điểm nhân sự", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))

        Text(
            "Hôm nay: ${todayDDMMYYYY()}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {

            // ✅ Dropdown chọn tháng
            ExposedDropdownMenuBox(
                expanded = monthExpanded,
                onExpandedChange = { monthExpanded = !monthExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = month,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = { Text("Tháng (yyyy-MM)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                // Material3 bản anh dùng: dùng DropdownMenu
                DropdownMenu(
                    expanded = monthExpanded,
                    onDismissRequest = { monthExpanded = false }
                ) {
                    monthOptions.forEach { m ->
                        DropdownMenuItem(
                            text = { Text(m) },
                            onClick = {
                                month = m
                                monthExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // ✅ vẫn giữ nút "Tải" nếu anh muốn bấm tay
            // (vì đã auto load theo month, nút này chỉ là "reload")
            Button(
                onClick = { vm.load(month) },
                enabled = !ui.loading
            ) {
                Text(if (ui.loading) "Đang tải..." else "Tải")
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item { Text("Danh sách nhân sự", fontWeight = FontWeight.Bold) }

            if (ui.loading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
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

            item {
                Spacer(Modifier.height(8.dp))
                TopCodesSection(
                    title = "🔥 Top lỗi tuần ${ui.weekRangeText}",
                    items = ui.topWeekCodes,
                    codeNameMap = codeNameMap
                )

            }

            item {
                TopCodesSection(
                    title = "📊 Top lỗi tháng",
                    items = ui.topMonthCodes,
                    codeNameMap = codeNameMap
                )
            }

            ui.error?.let { err ->
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Lỗi: $err", color = Color.Red)
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "DEBUG codesLoaded=$codesLoaded codesSize=${codes.size}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
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
    items: List<com.savani.hrscore.network.CodeCount>,
    codeNameMap: Map<String, String>
) {
    Text(title, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(6.dp))

    Text("DEBUG size=${items.size}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
    Spacer(Modifier.height(6.dp))

    if (items.isEmpty()) {
        Text("Chưa có dữ liệu", color = Color.Gray)
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.take(10).forEachIndexed { idx, it ->
            val name = codeNameMap[it.code].takeUnless { s -> s.isNullOrBlank() } ?: it.code

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${idx + 1}. $name")
                    Text("${it.count} lần", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ===== helpers =====

private fun currentMonthYYYYMM(): String {
    val cal = Calendar.getInstance()
    val y = cal.get(Calendar.YEAR)
    val m = cal.get(Calendar.MONTH) + 1
    return "%04d-%02d".format(y, m)
}

private fun todayDDMMYYYY(): String {
    val cal = Calendar.getInstance()
    val d = cal.get(Calendar.DAY_OF_MONTH)
    val m = cal.get(Calendar.MONTH) + 1
    val y = cal.get(Calendar.YEAR)
    return "%02d/%02d/%04d".format(d, m, y)
}

private fun buildRecentMonthsOptions(n: Int): List<String> {
    val cal = Calendar.getInstance()
    val out = ArrayList<String>(n)
    repeat(n) {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        out.add("%04d-%02d".format(y, m))
        cal.add(Calendar.MONTH, -1)
    }
    return out
}

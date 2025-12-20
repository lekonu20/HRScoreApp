package com.savani.hrscore.ui.staff

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.savani.hrscore.Constants
import com.savani.hrscore.network.CodeItem
import com.savani.hrscore.network.RetrofitClient
import kotlinx.coroutines.launch

class StaffDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val staffId = intent.getStringExtra("staffId") ?: ""
        val staffName = intent.getStringExtra("staffName") ?: ""

        setContent {
            MaterialTheme {
                StaffDetailScreen(
                    staffId = staffId,
                    staffName = staffName
                )
            }
        }
    }
}

@Composable
private fun StaffDetailScreen(
    staffId: String,
    staffName: String
) {
    val scope = rememberCoroutineScope()

    var month by remember { mutableStateOf(currentMonthYYYYMM()) }
    var score by remember { mutableStateOf<Double?>(null) }

    var codes by remember { mutableStateOf<List<CodeItem>>(emptyList()) }
    var selectedCode by remember { mutableStateOf<CodeItem?>(null) }
    var expanded by remember { mutableStateOf(false) }

    var countText by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // Load codes + score
    LaunchedEffect(Unit) {
        loading = true
        try {
            val codesRes = RetrofitClient.api.getCodes()
            if (!codesRes.ok) throw Exception(codesRes.message ?: "Load codes failed")
            codes = codesRes.data

            val scoreRes = RetrofitClient.api.getScore(staffId = staffId, month = month)
            if (!scoreRes.ok) throw Exception(scoreRes.message ?: "Load score failed")
            score = scoreRes.data?.score
        } catch (e: Exception) {
            message = "❌ ${e.message}"
        } finally {
            loading = false
        }
    }

    val count = countText.toIntOrNull() ?: 1
    val totalDelta = (selectedCode?.point ?: 0.0) * count

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nhân viên: $staffName", style = MaterialTheme.typography.titleMedium)
        Text("StaffId: $staffId", style = MaterialTheme.typography.bodySmall)

        // Month
        OutlinedTextField(
            value = month,
            onValueChange = {
                month = it.trim()
            },
            label = { Text("Tháng (yyyy-MM)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Score
        Text(
            text = "Điểm tháng: ${score?.toString() ?: "—"}",
            style = MaterialTheme.typography.titleSmall
        )

        // Code dropdown
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = codes.isNotEmpty()
        ) {
            Text(
                selectedCode?.let { "${it.code} – ${it.desc} (${formatPoint(it.point)})" }
                    ?: "Chọn CODE từ Codes"
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            codes.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text("${item.code} – ${item.desc} (${formatPoint(item.point)})")
                    },
                    onClick = {
                        selectedCode = item
                        expanded = false
                    }
                )
            }
        }

        // Count
        OutlinedTextField(
            value = countText,
            onValueChange = { countText = it },
            label = { Text("Số lần (count)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Note
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Ghi chú") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Delta (điểm cộng/trừ): ${formatPoint(totalDelta)}")

        Button(
            onClick = {
                if (selectedCode == null) {
                    message = "⚠️ Chưa chọn CODE"
                    return@Button
                }
                val c = countText.toIntOrNull()
                if (c == null || c <= 0) {
                    message = "⚠️ Count phải là số > 0"
                    return@Button
                }

                scope.launch {
                    loading = true
                    message = ""
                    try {
                        val res = RetrofitClient.api.applyCode(
                            staffId = staffId,
                            month = month,
                            code = selectedCode!!.code,
                            count = c,
                            note = note,
                            role = Constants.ROLE,
                            actor = Constants.ACTOR,
                            key = Constants.APPLY_KEY
                        )
                        if (!res.ok) throw Exception(res.message ?: "Apply failed")

                        // Update UI
                        score = res.data?.score
                        message = "✅ Đã áp dụng: ${selectedCode!!.code} (${formatPoint(res.data?.delta ?: 0.0)})"
                        // reset input
                        note = ""
                        countText = "1"
                        selectedCode = null
                    } catch (e: Exception) {
                        message = "❌ ${e.message}"
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(if (loading) "Đang xử lý..." else "ÁP DỤNG")
        }

        if (message.isNotBlank()) {
            Text(message)
        }

        Divider()

        // Reload score button (optional)
        OutlinedButton(
            onClick = {
                scope.launch {
                    loading = true
                    message = ""
                    try {
                        val scoreRes = RetrofitClient.api.getScore(staffId = staffId, month = month)
                        if (!scoreRes.ok) throw Exception(scoreRes.message ?: "Load score failed")
                        score = scoreRes.data?.score
                        message = "✅ Đã tải lại điểm"
                    } catch (e: Exception) {
                        message = "❌ ${e.message}"
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text("Tải lại điểm tháng")
        }
    }
}

// Helpers
private fun currentMonthYYYYMM(): String {
    val cal = java.util.Calendar.getInstance()
    val y = cal.get(java.util.Calendar.YEAR)
    val m = cal.get(java.util.Calendar.MONTH) + 1
    return "%04d-%02d".format(y, m)
}

private fun formatPoint(p: Double): String {
    // gọn: nếu là số nguyên thì không show .0
    val asInt = p.toInt()
    return if (kotlin.math.abs(p - asInt.toDouble()) < 1e-9) asInt.toString() else p.toString()
}

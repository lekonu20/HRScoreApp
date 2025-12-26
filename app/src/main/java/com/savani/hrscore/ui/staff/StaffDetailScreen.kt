package com.savani.hrscore.ui.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.savani.hrscore.Constants
import com.savani.hrscore.data.KeyStore
import com.savani.hrscore.model.ApplyLog
import com.savani.hrscore.model.CodeItem
import com.savani.hrscore.network.RetrofitClient
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun StaffDetailScreen(
    staffId: String,
    staffName: String,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val store = remember { KeyStore(ctx) }

    // ✅ lấy KEY manager đã lưu local (KHÔNG collect vô tận trong LaunchedEffect)
    val managerKey by store.managerKeyFlow.collectAsState(initial = null)

    // ✅ tháng backend yyyy-MM (tạm: tháng hiện tại)
    var month by remember { mutableStateOf(currentMonthYYYYMM()) }

    // ===== UI state: score + logs =====
    var loadingDetail by remember { mutableStateOf(true) }
    var detailMsg by remember { mutableStateOf("") }
    var scoreText by remember { mutableStateOf("") }
    var logs by remember { mutableStateOf<List<ApplyLog>>(emptyList()) }

    // ===== codes + apply =====
    var showLogDialog by remember { mutableStateOf(false) }

    var loadingCodes by remember { mutableStateOf(true) }
    var codesMsg by remember { mutableStateOf("") }
    var codes by remember { mutableStateOf<List<CodeItem>>(emptyList()) }

    var quickSubmitting by remember { mutableStateOf(false) }
    var quickMsg by remember { mutableStateOf("") }

    // ✅ actor/role/key: dùng Constants để đúng với hệ thống hiện tại
    val actor = Constants.ACTOR
    val role = Constants.ROLE

    fun requireKey(): String? {
        val k = managerKey?.trim()
        if (k.isNullOrBlank()) {
            quickMsg = "❌ Chưa có KEY manager (vào màn nhập KEY)"
            return null
        }
        return k
    }

    fun refreshDetail() {
        scope.launch {
            loadingDetail = true
            detailMsg = ""
            try {
                // ✅ 1) lấy score tháng (action=getscore)
                val scoreRes = RetrofitClient.api.getScore(staffId = staffId, month = month)
                if (!scoreRes.ok) throw Exception(scoreRes.message ?: "Get score failed")
                scoreText = (scoreRes.score ?: "").toString()

                // ✅ 2) lấy log (action=getlog)
                val logRes = RetrofitClient.api.getLog(staffId = staffId, month = month)
                if (!logRes.ok) throw Exception(logRes.message ?: "Get log failed")
                logs = logRes.data
            } catch (e: Exception) {
                detailMsg = "❌ ${e.message}"
            } finally {
                loadingDetail = false
            }
        }
    }

    // ✅ load detail khi vào màn / đổi tháng
    LaunchedEffect(staffId, month) {
        refreshDetail()
    }

    // ✅ load codes 1 lần cho màn này
    LaunchedEffect(Unit) {
        loadingCodes = true
        codesMsg = ""
        try {
            val res = RetrofitClient.api.getCodes()
            if (!res.ok) throw Exception(res.message ?: "Load codes failed")
            codes = res.data
        } catch (e: Exception) {
            codesMsg = "❌ ${e.message}"
        } finally {
            loadingCodes = false
        }
    }

    fun quickApply(delta: Int) {
        val k = requireKey() ?: return

        // ✅ tìm 1 code có point đúng +1 hoặc -1
        val target = codes.firstOrNull { it.point?.toString()?.toIntOrNull() == delta }

        if (target == null) {
            quickMsg = "❌ Không tìm thấy mã có point = $delta trong sheet Codes"
            return
        }

        quickSubmitting = true
        quickMsg = ""

        scope.launch {
            try {
                val res = RetrofitClient.api.applyLog(
                    staffId = staffId,
                    month = month,
                    code = target.code,
                    count = 1,
                    note = "",
                    actor = actor,
                    role = role,
                    key = k
                )
                if (!res.ok) throw Exception(res.message ?: "Apply failed")

                // ✅ update nhanh score nếu backend trả score
                res.score?.let { scoreText = it.toString() }

                quickMsg = "✅ Đã ghi nhanh ${target.code} (${target.point})"
                refreshDetail()
            } catch (e: Exception) {
                quickMsg = "❌ ${e.message}"
            } finally {
                quickSubmitting = false
            }
        }
    }

    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Chi tiết nhân sự", style = MaterialTheme.typography.titleLarge)
            Text("ID: $staffId", style = MaterialTheme.typography.bodyMedium)
            Text("Tên: $staffName", style = MaterialTheme.typography.bodyMedium)
            Text("Tháng: $month", style = MaterialTheme.typography.bodySmall)

            if (managerKey.isNullOrBlank()) {
                Text(
                    "⚠️ Chưa có KEY manager. Không thể ghi lỗi/điểm.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (loadingDetail) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                if (detailMsg.isNotBlank()) Text(detailMsg)
            } else {
                if (detailMsg.isNotBlank()) Text(detailMsg)

                // ✅ hiển thị điểm hiện tại
                Text(
                    text = "Điểm tháng hiện tại: ${if (scoreText.isBlank()) "—" else scoreText}",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(4.dp))

                Text("Lịch sử điểm/lỗi", style = MaterialTheme.typography.titleSmall)
                if (logs.isEmpty()) {
                    Text("Chưa có log", style = MaterialTheme.typography.bodySmall)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        items(logs) { it ->
                            // Hiển thị an toàn: dựa vào field phổ biến của ApplyLog
                            // Nếu model ApplyLog của anh khác tên field, sửa ở đây 1 lần là xong.
                            val date = it.date ?: it.createdAt ?: ""
                            val code = it.code ?: ""
                            val count = it.count?.toString() ?: ""
                            val point = it.point?.toString() ?: ""
                            val delta = it.delta?.toString() ?: ""
                            val by = it.by ?: ""

                            Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                Text("$date  •  $code  •  x$count", style = MaterialTheme.typography.bodyMedium)
                                Text("point: $point  •  delta: $delta  •  by: $by", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            if (codesMsg.isNotBlank()) Text(codesMsg)
            if (quickMsg.isNotBlank()) Text(quickMsg)

            // ✅ Ghi nhanh +/-1
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { quickApply(-1) },
                    enabled = !loadingCodes && !quickSubmitting && !managerKey.isNullOrBlank(),
                    modifier = Modifier.weight(1f)
                ) { Text("−1 nhanh") }

                Button(
                    onClick = { quickApply(+1) },
                    enabled = !loadingCodes && !quickSubmitting && !managerKey.isNullOrBlank(),
                    modifier = Modifier.weight(1f)
                ) { Text("+1 nhanh") }
            }

            // ✅ nút thao tác
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) { Text("Quay lại") }

                Button(
                    onClick = { showLogDialog = true },
                    enabled = !managerKey.isNullOrBlank(),
                    modifier = Modifier.weight(1f)
                ) { Text("Ghi lỗi/điểm") }
            }
        }
    }

    if (showLogDialog) {
        ApplyLogDialog(
            staffId = staffId,
            staffName = staffName,
            keyFromStore = managerKey,
            actor = actor,
            role = role,
            onDismiss = { showLogDialog = false },
            onApplied = {
                // ✅ ghi xong thì refresh lại điểm + log
                refreshDetail()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyLogDialog(
    staffId: String,
    staffName: String,
    keyFromStore: String?,
    actor: String,
    role: String,
    onDismiss: () -> Unit,
    onApplied: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val month = remember { currentMonthYYYYMM() }

    var loadingCodes by remember { mutableStateOf(true) }
    var codesMsg by remember { mutableStateOf("") }
    var codes by remember { mutableStateOf<List<CodeItem>>(emptyList()) }

    var expanded by remember { mutableStateOf(false) }
    var selectedCode by remember { mutableStateOf<CodeItem?>(null) }

    var countText by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }

    var submitting by remember { mutableStateOf(false) }
    var submitMsg by remember { mutableStateOf("") }

    val key = keyFromStore?.trim().orEmpty()

    LaunchedEffect(Unit) {
        loadingCodes = true
        codesMsg = ""
        try {
            val res = RetrofitClient.api.getCodes()
            if (!res.ok) throw Exception(res.message ?: "Load codes failed")
            codes = res.data
        } catch (e: Exception) {
            codesMsg = "❌ ${e.message}"
        } finally {
            loadingCodes = false
        }
    }

    val count = countText.toIntOrNull() ?: 0
    val canSubmit = selectedCode != null && count > 0 && !submitting && !loadingCodes && key.isNotBlank()

    AlertDialog(
        onDismissRequest = { if (!submitting) onDismiss() },
        title = { Text("Ghi lỗi/điểm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Nhân sự: $staffName • $staffId", style = MaterialTheme.typography.bodySmall)
                Text("Tháng: $month", style = MaterialTheme.typography.bodySmall)

                if (key.isBlank()) {
                    Text(
                        "❌ Chưa có KEY manager. Vui lòng nhập KEY ở màn cổng.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (loadingCodes) {
                    Text("Đang tải danh sách lỗi…")
                } else if (codesMsg.isNotBlank()) {
                    Text(codesMsg)
                } else {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCode?.let { "${it.code} - ${it.desc ?: ""} (${it.point})" } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Chọn mã lỗi / thưởng") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            codes.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text("${item.code} - ${item.desc ?: ""} (${item.point})") },
                                    onClick = {
                                        selectedCode = item
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = countText,
                        onValueChange = { countText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Số lần") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Ghi chú") },
                        singleLine = true
                    )
                }

                if (submitMsg.isNotBlank()) {
                    Text(submitMsg)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSubmit,
                onClick = {
                    val code = selectedCode!!.code
                    val c = count
                    submitting = true
                    submitMsg = ""

                    scope.launch {
                        try {
                            val res = RetrofitClient.api.applyLog(
                                staffId = staffId,
                                month = month,
                                code = code,
                                count = c,
                                note = note,
                                actor = actor,
                                role = role,
                                key = key
                            )
                            if (!res.ok) throw Exception(res.message ?: "Apply failed")
                            submitMsg = "✅ Đã ghi $code x$c"
                            onApplied()
                            onDismiss()
                        } catch (e: Exception) {
                            submitMsg = "❌ ${e.message}"
                        } finally {
                            submitting = false
                        }
                    }
                }
            ) { Text(if (submitting) "Đang ghi..." else "Ghi") }
        },
        dismissButton = {
            TextButton(
                enabled = !submitting,
                onClick = onDismiss
            ) { Text("Hủy") }
        }
    )
}

/* ===== helper month ===== */
private fun currentMonthYYYYMM(): String {
    val cal = Calendar.getInstance()
    return "%04d-%02d".format(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1
    )
}

package com.savani.hrscore.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.savani.hrscore.Constants
import com.savani.hrscore.data.KeyStore
import com.savani.hrscore.model.ApplyLog
import com.savani.hrscore.model.CodeItem
import com.savani.hrscore.network.RetrofitClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.util.Calendar

/* =========================
   STAFF DETAIL SCREEN
========================= */
@Composable
fun StaffDetailScreen(
    staffId: String,
    staffName: String,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val store = remember { KeyStore(ctx) }
    val managerKey by store.managerKeyFlow.collectAsState(initial = null)

    val month = remember { currentMonthYYYYMM() }

    var loading by remember { mutableStateOf(true) }
    var msg by remember { mutableStateOf("") }
    var scoreText by remember { mutableStateOf("") }
    var logs by remember { mutableStateOf<List<ApplyLog>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }

    fun loadDetail() {
        scope.launch {
            loading = true
            msg = ""
            try {
                val sRes = RetrofitClient.api.getScore(staffId = staffId, month = month)
                if (!sRes.ok) throw Exception(sRes.message ?: "Get score failed")
                scoreText = (sRes.score ?: "").toString()

                val lRes = RetrofitClient.api.getLog(staffId = staffId, month = month)
                if (!lRes.ok) throw Exception(lRes.message ?: "Get log failed")
                logs = lRes.data
            } catch (e: Exception) {
                msg = "❌ ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadDetail() }

    val scoreVal = scoreText.toDoubleOrNull()
    val scoreColor =
        if (scoreVal == null) MaterialTheme.colorScheme.onSurface
        else if (scoreVal < 90) MaterialTheme.colorScheme.error
        else Color(0xFF2E7D32)

    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ===== HEADER =====
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val firstChar = staffName.trim().take(1).uppercase()
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(MaterialTheme.shapes.large)
                            .background(Color(0xFF6A4BC3)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            firstChar,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Chi tiết nhân sự", style = MaterialTheme.typography.titleLarge)
                        Text(
                            staffName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text("ID: $staffId", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.large)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(month, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ===== SCORE =====
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Điểm tháng hiện tại", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = if (scoreText.isBlank()) "—" else scoreText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                    }
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(22.dp))
                }
            }

            if (msg.isNotBlank()) {
                Text(msg, color = MaterialTheme.colorScheme.error)
            }

            // ===== LOGS =====
            Text("Lịch sử điểm/lỗi", style = MaterialTheme.typography.titleMedium)

            Card(modifier = Modifier.fillMaxWidth()) {
                when {
                    loading -> {
                        Column(
                            Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(8.dp))
                            Text("Đang tải…", color = Color.Gray)
                        }
                    }
                    logs.isEmpty() -> {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Chưa có log", fontWeight = FontWeight.SemiBold)
                            Text("Bấm “Ghi lỗi/điểm” để thêm.", color = Color.Gray)
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 240.dp, max = 420.dp)
                                .padding(vertical = 6.dp)
                        ) {
                            items(logs) { lg ->
                                LogRowPretty(lg)
                                Divider(modifier = Modifier.padding(horizontal = 12.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ===== BOTTOM =====
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) { Text("Quay lại") }

                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.weight(1f)
                ) { Text("Ghi lỗi/điểm") }
            }
        }
    }

    if (showDialog) {
        ApplyLogDialog(
            staffId = staffId,
            staffName = staffName,
            month = month,
            keyFromStore = managerKey,
            actor = Constants.ACTOR,
            role = Constants.ROLE,
            onDismiss = { showDialog = false },
            onApplied = { loadDetail() }
        )
    }
}

/* =========================
   LOG ROW
========================= */
@Composable
private fun LogRowPretty(lg: ApplyLog) {
    val date = (lg.date ?: lg.createdAt ?: "").take(10)
    val code = (lg.code ?: "").trim()
    val count = lg.count ?: 1
    val delta = lg.delta ?: 0.0
    val isBad = delta < 0
    val deltaColor = if (isBad) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
    val deltaText = if (delta > 0) "+$delta" else "$delta"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (isBad) "⚠️" else "🎁", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(code, fontWeight = FontWeight.SemiBold)
            Text("$date • x$count • Δ $deltaText", color = deltaColor, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/* =========================
   APPLY LOG DIALOG (DIALOG PICKER - FIXED)
========================= */
@Composable
fun ApplyLogDialog(
    staffId: String,
    staffName: String,
    month: String,
    keyFromStore: String?,
    actor: String,
    role: String,
    onDismiss: () -> Unit,
    onApplied: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var loadingCodes by remember { mutableStateOf(true) }
    var codesMsg by remember { mutableStateOf("") }
    var codes by remember { mutableStateOf<List<CodeItem>>(emptyList()) }

    var selectedCode by remember { mutableStateOf<CodeItem?>(null) }
    var countText by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }

    var submitting by remember { mutableStateOf(false) }
    var submitMsg by remember { mutableStateOf("") }

    var showPicker by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }

    val key = keyFromStore?.trim().takeUnless { it.isNullOrBlank() } ?: Constants.APPLY_KEY

    LaunchedEffect(Unit) {
        loadingCodes = true
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
    val canSubmit = selectedCode != null && count > 0 && !submitting && !loadingCodes

    // ✅ mở picker an toàn (tránh dismiss ngay)
    val openPicker: () -> Unit = {
        scope.launch {
            yield()
            search = ""
            showPicker = true
        }
    }

    AlertDialog(
        onDismissRequest = { if (!submitting) onDismiss() },
        title = { Text("Ghi lỗi/điểm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Nhân sự: $staffName • $staffId", style = MaterialTheme.typography.bodySmall)
                Text("Tháng: $month", style = MaterialTheme.typography.bodySmall)

                if (loadingCodes) {
                    Text("Đang tải danh sách lỗi…")
                } else if (codesMsg.isNotBlank()) {
                    Text(codesMsg, color = MaterialTheme.colorScheme.error)
                } else {
                    OutlinedTextField(
                        value = selectedCode?.let { "${it.code} - ${it.desc ?: ""} (${it.point})" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Chọn mã lỗi / thưởng") },
                        trailingIcon = {
                            IconButton(onClick = openPicker) {
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Chọn mã")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = countText,
                        onValueChange = { countText = it.filter(Char::isDigit) },
                        label = { Text("Số lần") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Ghi chú") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (submitMsg.isNotBlank()) Text(submitMsg)
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSubmit,
                onClick = {
                    submitting = true
                    submitMsg = ""
                    scope.launch {
                        try {
                            val res = RetrofitClient.api.applyLog(
                                staffId = staffId,
                                month = month,
                                code = selectedCode!!.code,
                                count = count,
                                note = note,
                                actor = actor,
                                role = role,
                                key = key
                            )
                            if (!res.ok) throw Exception(res.message ?: "Apply failed")
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
            TextButton(enabled = !submitting, onClick = onDismiss) { Text("Hủy") }
        }
    )

    // ===== PICKER DIALOG =====
    if (showPicker) {
        val filtered = remember(codes, search) {
            val q = search.trim().lowercase()
            if (q.isBlank()) codes
            else codes.filter {
                it.code.lowercase().contains(q) ||
                        (it.desc ?: "").lowercase().contains(q)
            }
        }

        Dialog(
            onDismissRequest = { showPicker = false },
            properties = DialogProperties(
                dismissOnClickOutside = false,
                dismissOnBackPress = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Chọn mã lỗi / thưởng", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        label = { Text("Tìm theo mã / mô tả…") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider()

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                    ) {
                        items(filtered) { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCode = item
                                        showPicker = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text("${item.code} (${item.point})", fontWeight = FontWeight.SemiBold)
                                val desc = (item.desc ?: "").trim()
                                if (desc.isNotBlank()) {
                                    Text(desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                            Divider()
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showPicker = false }) { Text("Đóng") }
                    }
                }
            }
        }
    }
}

/* ===== helper month ===== */
private fun currentMonthYYYYMM(): String {
    val cal = Calendar.getInstance()
    return "%04d-%02d".format(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1
    )
}

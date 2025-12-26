package com.savani.hrscore.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.savani.hrscore.model.InventoryRow
import com.savani.hrscore.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun InventoryScreen() {
    val scope = rememberCoroutineScope()

    var q by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<InventoryRow>>(emptyList()) }

    fun normalizeQuery(s: String) = s.trim().uppercase()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("TỒN KHO", style = MaterialTheme.typography.titleLarge)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = q,
                onValueChange = { q = it },
                label = { Text("Nhập mã (gốc hoặc full)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(
                enabled = !loading,
                onClick = {
                    val query = normalizeQuery(q)
                    if (query.isBlank()) {
                        message = "⚠️ Nhập mã để tìm"
                        items = emptyList()
                        return@Button
                    }

                    scope.launch {
                        loading = true
                        message = ""
                        items = emptyList()
                        try {
                            val res = RetrofitClient.api.searchInventory(q = query)
                            if (!res.ok) throw Exception(res.message ?: "Search inventory failed")
                            items = res.data
                            if (items.isEmpty()) message = "Không tìm thấy kết quả."
                        } catch (e: Exception) {
                            message = "❌ ${e.message}"
                        } finally {
                            loading = false
                        }
                    }
                }
            ) { Text(if (loading) "..." else "Tìm") }
        }

        if (message.isNotBlank()) Text(message)

        Divider()

        if (items.isEmpty() && message.isBlank()) {
            Text("Nhập mã rồi bấm Tìm.", style = MaterialTheme.typography.bodyMedium)
        } else {
            items.forEach { it ->
                InventoryCard(it)
            }
        }
    }
}

@Composable
private fun InventoryCard(it: InventoryRow) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(it.code.ifBlank { "—" }, style = MaterialTheme.typography.titleMedium)

            val line1 = listOfNotNull(
                it.name?.takeIf { s -> s.isNotBlank() },
                it.color?.takeIf { s -> s.isNotBlank() }?.let { s -> "Màu: $s" },
                it.size?.takeIf { s -> s.isNotBlank() }?.let { s -> "Size: $s" },
            ).joinToString(" • ")
            if (line1.isNotBlank()) Text(line1, style = MaterialTheme.typography.bodyMedium)

            val line2 = listOfNotNull(
                it.stock?.let { s -> "Tồn: ${trimZero(s)}" },
                it.price?.let { p -> "Giá: ${trimZero(p)}" },
                it.location?.takeIf { s -> s.isNotBlank() }?.let { s -> "Kho: $s" }
            ).joinToString(" • ")
            if (line2.isNotBlank()) Text(line2, style = MaterialTheme.typography.bodyMedium)

            it.note?.takeIf { s -> s.isNotBlank() }?.let { n ->
                Text("Ghi chú: $n", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun trimZero(v: Double): String {
    val s = "%.2f".format(v)
    return s.trimEnd('0').trimEnd('.')
}

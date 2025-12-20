package com.savani.hrscore

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.savani.hrscore.network.RetrofitClient
import kotlinx.coroutines.launch

data class StaffItem(
    val id: String,
    val name: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MainScreen(
                    onOpenStaff = { staffId, staffName ->
                        startActivity(
                            Intent(this, com.savani.hrscore.ui.staff.StaffDetailActivity::class.java).apply {
                                putExtra("staffId", staffId)
                                putExtra("staffName", staffName)
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun MainScreen(onOpenStaff: (String, String) -> Unit) {
    val scope = rememberCoroutineScope()

    var staff by remember { mutableStateOf<List<StaffItem>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        loading = true
        try {
            val res = RetrofitClient.api.getStaff() // KHÔNG có url=
            if (!res.ok) throw Exception(res.message ?: "Load staff failed")
            staff = res.data.map { StaffItem(it.id, it.name) }
        } catch (e: Exception) {
            message = "❌ ${e.message}"
        } finally {
            loading = false
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Danh sách nhân viên", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (message.isNotBlank()) Text(message)

        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(staff) { s ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenStaff(s.id, s.name) }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(s.name, style = MaterialTheme.typography.titleSmall)
                        Text(s.id, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

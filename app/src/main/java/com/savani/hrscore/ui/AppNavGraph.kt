package com.savani.hrscore.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.savani.hrscore.ui.inventory.InventoryScreen
import com.savani.hrscore.ui.staff.StaffDashboardScreen
import com.savani.hrscore.ui.staff.StaffDetailScreen
import java.net.URLDecoder

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        // ✅ HOME
        composable("home") {
            HomeScreen(navController = navController)
        }

        // ✅ NHÂN SỰ
        composable("staff_dashboard") {
            StaffDashboardScreen(navController = navController)
        }

        // ✅ CHI TIẾT NHÂN SỰ
        composable(
            route = "staff_detail/{staffId}/{staffName}",
            arguments = listOf(
                navArgument("staffId") { type = NavType.StringType },
                navArgument("staffName") { type = NavType.StringType }
            )
        ) { entry ->

            val staffId = entry.arguments?.getString("staffId").orEmpty()
            val staffNameEncoded = entry.arguments?.getString("staffName").orEmpty()

            val staffName = runCatching {
                URLDecoder.decode(staffNameEncoded, "UTF-8")
            }.getOrDefault(staffNameEncoded)

            StaffDetailScreen(
                staffId = staffId,
                staffName = staffName,
                navController = navController
            )
        }

        // ✅ DOANH THU (tạm)
        composable("sales") {
            PlaceholderScreen("DOANH THU")
        }

        // ✅ TỒN KHO
        composable("inventory") {
            InventoryScreen()
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

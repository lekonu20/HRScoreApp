package com.savani.hrscore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savani.hrscore.model.StaffScoreRow
import com.savani.hrscore.network.CodeCount
import com.savani.hrscore.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StaffDashboardUiState(
    val loading: Boolean = false,
    val staffScores: List<StaffScoreRow> = emptyList(),
    val topWeekCodes: List<CodeCount> = emptyList(),
    val topMonthCodes: List<CodeCount> = emptyList(),
    val error: String? = null
)

class StaffDashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(StaffDashboardUiState())
    val uiState: StateFlow<StaffDashboardUiState> = _uiState.asStateFlow()

    fun load(month: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            try {
                val api = RetrofitClient.api

                // Chạy song song cho nhanh
                val dashDeferred = async { api.getDashboardMonth(month = month) }
                val weekDeferred = async { api.getTopCodes(month = month, range = "week") }
                val monthDeferred = async { api.getTopCodes(month = month, range = "month") }

                val dashRes = dashDeferred.await()
                val weekRes = weekDeferred.await()
                val monthRes = monthDeferred.await()

                if (!dashRes.ok) {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = "Dashboard API ok=false"
                    )
                    return@launch
                }

                val weekList: List<CodeCount> = if (weekRes.ok) weekRes.data else emptyList()
                val monthList: List<CodeCount> = if (monthRes.ok) monthRes.data else emptyList()

                _uiState.value = StaffDashboardUiState(
                    loading = false,
                    staffScores = dashRes.data,
                    topWeekCodes = weekList,
                    topMonthCodes = monthList,
                    error = null
                )

            } catch (e: Exception) {
                _uiState.value = StaffDashboardUiState(
                    loading = false,
                    staffScores = emptyList(),
                    topWeekCodes = emptyList(),
                    topMonthCodes = emptyList(),
                    error = e.message ?: "Lỗi tải dashboard"
                )
            }
        }
    }
}

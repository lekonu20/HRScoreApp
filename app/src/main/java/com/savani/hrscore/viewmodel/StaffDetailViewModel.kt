package com.savani.hrscore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savani.hrscore.model.StaffScoreRow
import com.savani.hrscore.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StaffDashboardUiState(
    val loading: Boolean = false,
    val staffScores: List<StaffScoreRow> = emptyList(),
    val error: String? = null
)

class StaffDashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(StaffDashboardUiState())
    val uiState: StateFlow<StaffDashboardUiState> = _uiState.asStateFlow()

    fun load(month: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            try {
                val res = RetrofitClient.api.getDashboardMonth(month = month)
                if (res.ok) {
                    _uiState.value = StaffDashboardUiState(
                        loading = false,
                        staffScores = res.data
                    )
                } else {
                    _uiState.value = StaffDashboardUiState(
                        loading = false,
                        error = "API ok=false"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = StaffDashboardUiState(
                    loading = false,
                    error = e.message ?: "Lỗi tải dashboard"
                )
            }
        }
    }
}

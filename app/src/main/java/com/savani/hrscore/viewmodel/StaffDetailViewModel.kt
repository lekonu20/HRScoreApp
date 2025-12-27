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
import java.util.Calendar

data class StaffDashboardUiState(
    val loading: Boolean = false,
    val staffScores: List<StaffScoreRow> = emptyList(),
    val topWeekCodes: List<CodeCount> = emptyList(),
    val topMonthCodes: List<CodeCount> = emptyList(),
    val weekRangeText: String = "", // ✅ hiển thị từ ngày nào → ngày nào
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
                val preferredWeek = weekOfMonthForSelectedMonth(month)

                val dashDeferred = async { api.getDashboardMonth(month = month) }
                val monthDeferred = async { api.getTopCodes(month = month, range = "month") }

                // ✅ Chỉ cần gọi đúng "tuần hiện tại" (không dò 1..6 nữa)
                val weekLogDeferred = async {
                    api.getLogWeek(month = month, week = preferredWeek, staffId = null)
                }

                val dashRes = dashDeferred.await()
                val monthRes = monthDeferred.await()
                val weekLogRes = weekLogDeferred.await()

                if (!dashRes.ok) {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = "Dashboard API ok=false"
                    )
                    return@launch
                }

                val monthList: List<CodeCount> =
                    if (monthRes.ok) monthRes.data else emptyList()

                // ✅ Range text luôn theo tuần hiện tại
                val weekRangeText = weekRangeText(month, preferredWeek)

                // ✅ Top tuần: nếu tuần này không có log thì list rỗng (đúng nghiệp vụ)
                val weekList: List<CodeCount> =
                    if (weekLogRes.ok) {
                        weekLogRes.data
                            .groupBy { it.code }
                            .map { (code, rows) ->
                                CodeCount(
                                    code = code,
                                    count = rows.sumOf { it.count }
                                )
                            }
                            .sortedByDescending { it.count }
                    } else {
                        emptyList()
                    }

                _uiState.value = StaffDashboardUiState(
                    loading = false,
                    staffScores = dashRes.data,
                    topWeekCodes = weekList,
                    topMonthCodes = monthList,
                    weekRangeText = weekRangeText,
                    error = null
                )

            } catch (e: Exception) {
                _uiState.value = StaffDashboardUiState(
                    loading = false,
                    staffScores = emptyList(),
                    topWeekCodes = emptyList(),
                    topMonthCodes = emptyList(),
                    weekRangeText = "",
                    error = e.message ?: "Lỗi tải dashboard"
                )
            }
        }
    }
}

/**
 * Tuần của tháng:
 * - Nếu tháng hiện tại → tuần hôm nay
 * - Nếu tháng khác → tuần 1
 */
private fun weekOfMonthForSelectedMonth(monthYYYYMM: String): Int {
    val y = monthYYYYMM.substring(0, 4).toIntOrNull()
        ?: Calendar.getInstance().get(Calendar.YEAR)
    val m = monthYYYYMM.substring(5, 7).toIntOrNull()
        ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)

    val now = Calendar.getInstance()
    val nowY = now.get(Calendar.YEAR)
    val nowM = now.get(Calendar.MONTH) + 1

    val day = if (y == nowY && m == nowM) now.get(Calendar.DAY_OF_MONTH) else 1

    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY
    cal.set(Calendar.YEAR, y)
    cal.set(Calendar.MONTH, m - 1)
    cal.set(Calendar.DAY_OF_MONTH, day)
    cal.set(Calendar.HOUR_OF_DAY, 12)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    return cal.get(Calendar.WEEK_OF_MONTH)
}

/**
 * ✅ Hiển thị range theo tuần trong tháng (Thứ 2 → CN)
 * VD: (23/12 → 29/12)
 */
private fun weekRangeText(monthYYYYMM: String, weekOfMonth: Int): String {
    val y = monthYYYYMM.substring(0, 4).toInt()
    val m = monthYYYYMM.substring(5, 7).toInt()

    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY

    // set về ngày 1 của tháng
    cal.set(Calendar.YEAR, y)
    cal.set(Calendar.MONTH, m - 1)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 12)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    // nhảy tới tuần cần lấy trong tháng
    cal.set(Calendar.WEEK_OF_MONTH, weekOfMonth)

    // về thứ 2
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val sd = cal.get(Calendar.DAY_OF_MONTH)
    val sm = cal.get(Calendar.MONTH) + 1

    // tới chủ nhật
    cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    val ed = cal.get(Calendar.DAY_OF_MONTH)
    val em = cal.get(Calendar.MONTH) + 1

    return "(${String.format("%02d/%02d", sd, sm)} → ${String.format("%02d/%02d", ed, em)})"
}

package com.savani.hrscore.network

import com.savani.hrscore.model.CodeItem
import com.savani.hrscore.model.InventoryRow
import com.savani.hrscore.model.StaffRow
import retrofit2.http.GET
import retrofit2.http.Query
import com.savani.hrscore.Constants
import com.savani.hrscore.network.ApiListResponse
import com.savani.hrscore.model.ApplyLog



data class ApiListResponse<T>(
    val ok: Boolean,
    val data: List<T> = emptyList(),
    val message: String? = null
)

data class ScoreResponse(
    val ok: Boolean,
    val staffId: String? = null,
    val month: String? = null,
    val score: Double? = null,
    val message: String? = null
)

data class ApplyLogResponse(
    val ok: Boolean,
    val staffId: String? = null,
    val month: String? = null,
    val code: String? = null,
    val count: Int? = null,
    val point: Double? = null,
    val delta: Double? = null,
    val score: Double? = null,
    val status: String? = null,
    val lockedAt: String? = null,
    val lockedBy: String? = null,
    val message: String? = null
)

// ===== NEW: Stats =====
data class CodeCount(
    val code: String = "",
    val count: Int = 0
)

data class WeekCount(
    val week: Int = 0,
    val count: Int = 0
)

data class StatsData(
    val topCodes: List<CodeCount> = emptyList(),
    val weekSummary: List<WeekCount> = emptyList()
)

data class StatsResponse(
    val ok: Boolean,
    val month: String? = null,
    val data: StatsData? = null,
    val message: String? = null
)

// ===== NEW: LogWeek =====
data class LogWeekRow(
    val date: String = "", // yyyy-MM-dd
    val code: String = "",
    val count: Int = 0
)

data class LogWeekResponse(
    val ok: Boolean,
    val month: String? = null,
    val week: Int? = null,
    val data: List<LogWeekRow> = emptyList(),
    val message: String? = null
)

interface ApiService {

    @GET("exec")
    suspend fun getStaff(
        @Query("action") action: String = "getStaff"
    ): ApiListResponse<StaffRow>

    @GET("exec")
    suspend fun getCodes(
        @Query("action") action: String = "getCodes"
    ): ApiListResponse<CodeItem>

    @GET("exec")
    suspend fun getScore(
        @Query("action") action: String = "getScore",
        @Query("staffId") staffId: String,
        @Query("month") month: String
    ): ScoreResponse

    @GET("exec")
    suspend fun applyLog(
        @Query("action") action: String = "applyLog",
        @Query("staffId") staffId: String,
        @Query("month") month: String,
        @Query("code") code: String,
        @Query("count") count: Int,
        @Query("note") note: String,
        @Query("actor") actor: String,
        @Query("role") role: String,
        @Query("key") key: String
    ): ApplyLogResponse

    // ====== CŨ: getLog (nặng) - giữ để xem chi tiết khi cần ======
    @GET("exec")
    suspend fun getLog(
        @Query("action") action: String = "getlog",
        @Query("staffId") staffId: String,
        @Query("month") month: String
    ): ApiListResponse<ApplyLog>


    // ====== NEW: stats (nhẹ, dùng cho dashboard) ======
    @GET("exec")
    suspend fun getStats(
        @Query("action") action: String = "stats",
        @Query("month") month: String
    ): StatsResponse

    // ====== NEW: logWeek (nhẹ, dùng khi bấm vào 1 tuần) ======
    @GET("exec")
    suspend fun getLogWeek(
        @Query("action") action: String = "logWeek",
        @Query("month") month: String,
        @Query("week") week: Int,
        @Query("staffId") staffId: String? = null
    ): LogWeekResponse

    @GET("exec")
    suspend fun applyPoint(
        @Query("action") action: String = "apply",
        @Query("key") key: String,
        @Query("staff") staff: String,
        @Query("month") month: String,
        @Query("delta") delta: Int
    ): SimpleResponse

    data class SimpleResponse(
        val status: String,
        val message: String
    )

    @GET("exec")
    suspend fun searchInventory(
        @Query("action") action: String = "searchInventory",
        @Query("q") q: String
    ): ApiListResponse<InventoryRow>
    @GET("exec")
    suspend fun getDashboardMonth(
        @Query("action") action: String = "dashboardmonth",
        @Query("month") month: String,
        @Query("key") key: String = Constants.APPLY_KEY,
        @Query("role") role: String = Constants.ROLE,
        @Query("actor") actor: String = Constants.ACTOR
    ): DashboardMonthResponse
    @GET("exec")
    suspend fun getStaffLog(
        @Query("action") action: String = "getlog",
        @Query("staffId") staffId: String,
        @Query("month") month: String,
        @Query("key") key: String = Constants.APPLY_KEY,
        @Query("role") role: String = Constants.ROLE,
        @Query("actor") actor: String = Constants.ACTOR
    ): GetLogResponse

}

package com.savani.hrscore.network

import retrofit2.http.GET
import retrofit2.http.Query

// Response wrappers
data class ApiListResponse<T>(
    val ok: Boolean,
    val data: List<T> = emptyList(),
    val message: String? = null
)

data class ApiObjResponse<T>(
    val ok: Boolean,
    val data: T? = null,
    val message: String? = null
)

// Models
data class CodeItem(
    val code: String,
    val desc: String,
    val point: Double
)

data class ScoreResponse(
    val staffId: String,
    val month: String,
    val score: Double
)

data class ApplyResponse(
    val staffId: String,
    val month: String,
    val code: String,
    val count: Int,
    val point: Double,
    val delta: Double,
    val score: Double
)

interface ApiService {

    @GET("exec")
    suspend fun getCodes(
        @Query("action") action: String = "codes"
    ): ApiListResponse<CodeItem>
    data class StaffDto(
        val id: String,
        val name: String
    )

    @GET("exec")
    suspend fun getStaff(
        @Query("action") action: String = "staff"
    ): ApiListResponse<StaffDto>

    @GET("exec")
    suspend fun getScore(
        @Query("action") action: String = "score",
        @Query("staffId") staffId: String,
        @Query("month") month: String
    ): ApiObjResponse<ScoreResponse>

    @GET("exec")
    suspend fun applyCode(
        @Query("action") action: String = "apply",
        @Query("staffId") staffId: String,
        @Query("month") month: String,
        @Query("code") code: String,
        @Query("count") count: Int,
        @Query("note") note: String,
        @Query("role") role: String,
        @Query("actor") actor: String,
        @Query("key") key: String
    ): ApiObjResponse<ApplyResponse>
}

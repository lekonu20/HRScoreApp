package com.savani.hrscore.network

/* ========= STAFF ========= */

data class StaffItem(
    val id: String,
    val name: String
)

data class StaffResponse(
    val ok: Boolean,
    val message: String? = null,
    val data: List<StaffItem> = emptyList()
)

/* ========= SCORE ========= */

/* ========= LOGS ========= */

data class ApplyLog(
    val date: String,     // ví dụ: 2025-12-21
    val staffId: String,  // ví dụ: SVN04951
    val code: String,     // ví dụ: L1
    val count: Int        // số lần
)

data class LogsResponse(
    val ok: Boolean,
    val message: String? = null,
    val data: List<ApplyLog> = emptyList()
)

/* ========= CODES ========= */

data class CodeItem(
    val code: String,
    val name: String,
    val point: Double
)

data class CodesResponse(
    val ok: Boolean,
    val message: String? = null,
    val data: List<CodeItem> = emptyList()
)

/* ========= APPLY ========= */

data class ApplyResponse(
    val ok: Boolean,
    val message: String? = null
)

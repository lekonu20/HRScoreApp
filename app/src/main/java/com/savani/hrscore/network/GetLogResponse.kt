package com.savani.hrscore.network

import com.savani.hrscore.model.ApplyLog

data class GetLogResponse(
    val ok: Boolean,
    val staffId: String,
    val month: String,
    val score: String,
    val data: List<ApplyLog>
)

package com.savani.hrscore.network

import com.savani.hrscore.model.StaffScoreRow

data class DashboardMonthResponse(
    val ok: Boolean,
    val month: String,
    val status: String,
    val lockedAt: String?,
    val lockedBy: String?,
    val data: List<StaffScoreRow>
)

package com.savani.hrscore.model

data class ApplyLog(
    val date: String? = null,
    val createdAt: String? = null,

    val code: String? = null,
    val count: Int? = null,

    val point: Double? = null,
    val delta: Double? = null,

    val by: String? = null,
    val note: String? = null
)

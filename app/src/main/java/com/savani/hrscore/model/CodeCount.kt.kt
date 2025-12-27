package com.savani.hrscore.model

data class CodeCount(
    val code: String = "",
    val count: Int = 0,
    val point: Double? = null // nếu backend có trả điểm/point từng code
)

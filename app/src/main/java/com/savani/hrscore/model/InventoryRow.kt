package com.savani.hrscore.model

data class InventoryRow(
    val code: String = "",          // mã (có thể full / mã gốc)
    val name: String? = null,       // tên sp (nếu có)
    val color: String? = null,      // màu (nếu có)
    val size: String? = null,       // size (nếu có)
    val stock: Double? = null,      // tồn (nếu có)
    val price: Double? = null,      // giá (nếu có)
    val location: String? = null,   // kho/cửa hàng (nếu có)
    val note: String? = null        // ghi chú (nếu có)
)

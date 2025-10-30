package com.zynt.sumviltadconnect.data.model

data class PaginatedCropHealthResponse(
    val data: List<CropHealthRecord>,
    val message: String,
    val count: Int,
    val current_page: Int,
    val last_page: Int,
    val per_page: Int,
    val total: Int,
    val from: Int?,
    val to: Int?,
    val has_more_pages: Boolean
)

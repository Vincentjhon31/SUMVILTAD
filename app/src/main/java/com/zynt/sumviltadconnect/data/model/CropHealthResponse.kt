package com.zynt.sumviltadconnect.data.model

import com.google.gson.annotations.SerializedName

data class CropHealthResponse(
    val data: List<CropHealthRecord>,
    val message: String,
    val count: Int,
    val success: Boolean = true
)

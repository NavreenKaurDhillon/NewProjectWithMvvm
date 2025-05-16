package com.live.humanmesh.model

data class BannersListResponse(
    val body: List<BannerItem>,
    val code: Int,
    val message: String,
    val success: Int
)
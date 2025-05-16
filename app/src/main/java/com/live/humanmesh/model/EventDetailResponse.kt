package com.live.humanmesh.model

data class EventDetailResponse(
    val body: BodyX,
    val code: Int,
    val message: String,
    val success: Int
)
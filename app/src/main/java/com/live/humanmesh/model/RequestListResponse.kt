package com.live.humanmesh.model

data class RequestListResponse(
    val body: List<EventBody>,
    val code: Int,
    val message: String,
    val success: Int
)
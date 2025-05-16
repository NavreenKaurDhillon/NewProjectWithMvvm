package com.live.humanmesh.model

data class FaqResponse(
    val body: List<Faq>,
    val code: Int,
    val message: String,
    val success: Int
)
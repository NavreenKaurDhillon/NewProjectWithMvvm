package com.live.humanmesh.model

data class ProfileResponse(
    val body: UserProfileBody,
    val code: Int,
    val message: String,
    val success: Int
)
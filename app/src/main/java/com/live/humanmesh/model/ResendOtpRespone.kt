package com.live.humanmesh.model

data class ResendOtpRespone(
    val body: SignupBody,
    val code: Int,
    val message: String,
    val success: Int
)
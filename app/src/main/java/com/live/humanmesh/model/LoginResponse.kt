package com.live.humanmesh.model

data class LoginResponse(
    val body: SignupBody,
    val code: Int,
    val message: String,
    val success: Int
)
package com.live.humanmesh.model

data class ContactUsResponse(
    val body: ContactUsBody,
    val code: Int,
    val message: String,
    val success: Int
){

    data class ContactUsBody(
        val country_code: String,
        val createdAt: String,
        val deletedAt: Any,
        val email: String,
        val id: Int,
        val message: String,
        val name: String,
        val phone_number: Long,
        val updatedAt: String
    )
}
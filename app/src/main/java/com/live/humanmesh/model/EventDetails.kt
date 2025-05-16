package com.live.humanmesh.model

data class EventDetails(
    val createdAt: String,
    val description: String,
    val end_date: Any,
    val end_time: Any,
    val id: Int,
    val image: String,
    val latitude: String,
    val location: String,
    val longitude: String,
    val start_date: Any,
    val start_time: Any,
    val status: String,
    val tag: String,
    val title: String,
    val type: String,
    val updatedAt: String,
    val user_id: Int
)
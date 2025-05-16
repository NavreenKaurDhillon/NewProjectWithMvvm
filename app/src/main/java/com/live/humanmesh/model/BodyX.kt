package com.live.humanmesh.model

data class BodyX(
    val Request: Request,
    val createdAt: String,
    val description: String,
    val end_date: Any?=null,
    val end_time: Any?=null,
    val id: Int,
    val image: String,
    val latitude: String,
    val location: String,
    val longitude: String,
    val requestDetails: List<RequestDetailX>,
    val start_date: Any?=null,
    val start_time: Any?=null,
    val status: String,
    val tag: String,
    val title: String,
    val type: String,
    val updatedAt: String,
    val user_id: Int
)
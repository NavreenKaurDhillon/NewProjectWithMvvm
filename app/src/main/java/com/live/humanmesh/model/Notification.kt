package com.live.humanmesh.model

data class Notification(
    val createdAt: String,
    val title: String,
    val deletedAt: Any,
    val event_id: Int,
    val id: Int,
    val is_read: Int,
    val message: String,
    val notification_type: Int,
    val receiver_id: Int,
    val request_type: Int,
    val sender: Sender,
    val sender_id: Int,
    val type: String,
    val updatedAt: String
)
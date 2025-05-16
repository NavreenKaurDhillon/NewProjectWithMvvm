package com.live.humanmesh.model

data class RequestDetailX(
    val createdAt: String,
    val event_id: Int,
    val id: Int,
    val receiverDetails: ReceiverDetailsX,
    val receiver_id: Int,
    val senderDetails: SenderDetailsX,
    val sender_id: Int,
    val status: Int,
    val updatedAt: String
)
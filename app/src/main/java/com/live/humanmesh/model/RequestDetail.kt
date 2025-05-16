package com.live.humanmesh.model

data class RequestDetail(
    val createdAt: String,
    val event_id: Int,
    val status: Int,
    val id: Int,
    val receiverDetails: ReceiverDetails,
    val receiver_id: Int,
    val senderDetails: SenderDetails,
    val sender_id: Int,
    val updatedAt: String
)
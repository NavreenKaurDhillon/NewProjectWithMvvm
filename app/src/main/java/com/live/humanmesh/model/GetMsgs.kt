package com.live.humanmesh.model

data class GetMsgs(
    val chatConstantId: Int,
    val createdAt: String,
    val id: Int,
    val is_read: String,
    val message: String,
    val msgType: Int,
    val receiverId: Int,
    val receiverImage: String,
    val receiverName: String,
    val senderId: Int,
    val senderImage: String,
    val senderName: String,
    val thumbnail: String,
    val updatedAt: String
)
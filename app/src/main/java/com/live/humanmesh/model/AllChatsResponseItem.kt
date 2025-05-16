package com.live.humanmesh.model

data class AllChatsResponseItem(
    val count: Int,
    val createdAt: String,
    val deletedLastMessageId: Int,
    val gender: String,
    val id: Int,
    val lastMessageId: Int,
    val last_message: String,
    val messageType: Int,
    val photoPermission: Int,
    val profileVisitPermission: Int,
    val account_public: String,
    val receiverId: Int,
    val receiverImage: String,
    val receiverName: String,
    val senderId: Int,
    val updatedAt: String
)
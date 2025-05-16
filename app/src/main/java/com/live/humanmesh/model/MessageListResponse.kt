package com.live.humanmesh.model

data class MessageListResponse(
    val isBlockByMe: Int,
    val isBlockByOther: Int,
    val messageList: List<Message>,
    val photoPermission: Int,
    val profileVisitPermission: Int,
    val receiverDetails: ReceiverDetailsXX,
    val remaining_words: Int,
    val status: Int
)
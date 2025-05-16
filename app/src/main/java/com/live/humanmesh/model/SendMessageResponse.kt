package com.live.humanmesh.model

data class SendMessageResponse(
    val getMsgs: GetMsgs,
    val remaining_words: Int,
    val status: Int,
    val message: String
)
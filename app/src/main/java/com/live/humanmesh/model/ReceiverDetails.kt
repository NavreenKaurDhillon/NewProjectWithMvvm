package com.live.humanmesh.model

data class ReceiverDetails(
    val account_public: String,
    val event_details_show: String,
    val nick_name_show: String,
    val gender: String?=null,
    val photo_visiblity: String
)
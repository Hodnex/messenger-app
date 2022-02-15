package com.hodnex.messengerapp.data

import java.text.SimpleDateFormat

data class Message (
    val fromId: String = "",
    val toId: String = "",
    val text: String = "",
    val messageTime: Long = System.currentTimeMillis()
) {
        val messageTimeFormatted : String
        get() = SimpleDateFormat("hh:mm").format(messageTime)
}
package com.hodnex.messengerapp.data

import com.google.type.DateTime
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

data class Message (
    val fromId: String = "",
    val toId: String = "",
    val text: String = "",
    val messageTime: Long = System.currentTimeMillis()
) {
        val messageTimeFormatted : String
        get() = SimpleDateFormat("hh:mm").format(messageTime)
}
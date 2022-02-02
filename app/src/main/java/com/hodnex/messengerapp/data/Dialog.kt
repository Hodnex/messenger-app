package com.hodnex.messengerapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Dialog(
    val uid: String = "",
    val name: String = "",
    val lastMessage: String = "",
    val time: Long = 0,
    val timeFormatted: String = ""
) : Parcelable
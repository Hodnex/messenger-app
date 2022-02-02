package com.hodnex.messengerapp.viewmodel

import androidx.lifecycle.*
import com.hodnex.messengerapp.data.DataRepository
import com.hodnex.messengerapp.data.Dialog
import com.hodnex.messengerapp.data.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: DataRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    val currentId = repository.currentUserId

    private val dialog = state.get<Dialog>("dialog")
    val title = state.get<String>("title")

    val messages = repository.getMessages(dialog!!.uid)

    fun sendMessage(text: String) = viewModelScope.launch {
        repository.addMessage(dialog!!.uid, text)
    }
}
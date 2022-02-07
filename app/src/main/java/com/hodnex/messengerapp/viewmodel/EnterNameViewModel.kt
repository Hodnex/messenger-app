package com.hodnex.messengerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hodnex.messengerapp.data.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnterNameViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    private val enterNameChannel = Channel<EnterNameEvent>()
    val enterNameEvent = enterNameChannel.receiveAsFlow()

    fun enterName(name: String) = viewModelScope.launch {
        repository.changeName(name)
        enterNameChannel.send(EnterNameEvent.NavigateToHomeScreen)
    }

    sealed class EnterNameEvent{
        object NavigateToHomeScreen : EnterNameEvent()
    }
}
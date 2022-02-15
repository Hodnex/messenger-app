package com.hodnex.messengerapp.viewmodel

import androidx.lifecycle.*
import com.hodnex.messengerapp.data.DataRepository
import com.hodnex.messengerapp.data.Dialog
import com.hodnex.messengerapp.data.Invitation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    val dialogs = repository.dialogs.asLiveData()
    val invitations = repository.invitations.asLiveData()

    private val dialogsChannel = Channel<HomeEvent>()
    val dialogsEvent = dialogsChannel.receiveAsFlow()


    fun onUserSelected(dialog: Dialog) = viewModelScope.launch {
        dialogsChannel.send(HomeEvent.NavigateToChatScreen(dialog))
    }

    fun addDialog(invitation: Invitation) = viewModelScope.launch {
        repository.addDialog(Dialog(name = invitation.senderName, uid = invitation.senderId))
        repository.deleteInvitation(invitation)
        dialogsChannel.send(HomeEvent.ShowAddDialogMessage)
    }

    fun deleteInvitation(invitation: Invitation) = viewModelScope.launch {
        repository.deleteInvitation(invitation)
        dialogsChannel.send(HomeEvent.ShowUndoDeleteInvitationMessage(invitation))
    }

    fun undoDeleteInvitation(invitation: Invitation) = viewModelScope.launch {
        repository.addInvitation(invitation)
    }

    fun inviteMember() = viewModelScope.launch {
        dialogsChannel.send(HomeEvent.NavigateToInviteMemberScreen)
    }

    sealed class HomeEvent {
        data class NavigateToChatScreen(val dialog: Dialog) : HomeEvent()
        object ShowAddDialogMessage : HomeEvent()
        object NavigateToInviteMemberScreen : HomeEvent()
        data class ShowUndoDeleteInvitationMessage(val invitation: Invitation) : HomeEvent()
    }
}
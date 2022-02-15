package com.hodnex.messengerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hodnex.messengerapp.data.DataRepository
import com.hodnex.messengerapp.data.Invitation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InviteMemberViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    private val inviteMemberChannel = Channel<InviteMemberEvent>()
    val inviteMemberEvent = inviteMemberChannel.receiveAsFlow()

    fun inviteMemberClick(email: String) = viewModelScope.launch {
        val sender = repository.currentUser
        val receiver = repository.findUserByEmail(email)
        val invitation = repository.findInvitation(receiver)
        val dialog = repository.findDialog(receiver)
        if (receiver != null && invitation == null && dialog == null) {
            repository.addInvitation(Invitation(sender.email, sender.name, sender.uid, receiver.uid))
            navigateBack("Member Invited")
        } else if (invitation != null){
            showErrorMessage("You are already invited by this player")
        } else if (dialog != null){
            showErrorMessage("You already have a dialog with this user")
        } else{
            showErrorMessage("User does not exist")
        }
    }

    private fun showErrorMessage(msg: String) = viewModelScope.launch {
        inviteMemberChannel.send(InviteMemberEvent.ShowErrorMessage(msg))
    }

    private fun navigateBack(msg: String) = viewModelScope.launch {
        inviteMemberChannel.send(InviteMemberEvent.NavigateBack(msg))
    }

    sealed class InviteMemberEvent{
        data class ShowErrorMessage(val msg: String) : InviteMemberEvent()
        data class NavigateBack(val msg: String) : InviteMemberEvent()
    }
}
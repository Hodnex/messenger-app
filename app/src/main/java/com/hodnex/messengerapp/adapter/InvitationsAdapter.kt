package com.hodnex.messengerapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hodnex.messengerapp.data.Invitation
import com.hodnex.messengerapp.databinding.ItemInvitationBinding

class InvitationsAdapter(private val listener: OnItemClickListener) :
    ListAdapter<Invitation, InvitationsAdapter.InvitationsViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitationsViewHolder {
        val binding =
            ItemInvitationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InvitationsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvitationsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class InvitationsViewHolder(private val binding: ItemInvitationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(invitation: Invitation) {
            binding.apply {
                textViewUserEmail.text = invitation.senderEmail
                textViewUserNameInvitation.text = invitation.senderName
                buttonAccept.setOnClickListener {
                    listener.onAcceptButtonClick(invitation)
                }
                buttonDecline.setOnClickListener {
                    listener.onDeclineButtonClick(invitation)
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onAcceptButtonClick(invitation: Invitation)
        fun onDeclineButtonClick(invitation: Invitation)
    }

    class DiffCallback : DiffUtil.ItemCallback<Invitation>() {

        override fun areItemsTheSame(oldItem: Invitation, newItem: Invitation) =
            oldItem.senderId == newItem.senderId && oldItem.receiverId == newItem.receiverId

        override fun areContentsTheSame(oldItem: Invitation, newItem: Invitation) = false
    }
}
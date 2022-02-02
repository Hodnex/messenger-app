package com.hodnex.messengerapp.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hodnex.messengerapp.data.Message
import com.hodnex.messengerapp.databinding.ItemMessageBinding

class ChatAdapter constructor(
    private val currentId: String
) : ListAdapter<Message, ChatAdapter.MessageViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class MessageViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.apply {
                if (currentId == message.fromId) {
                    constraintLayoutReceivedMessage.visibility = View.INVISIBLE
                    constraintLayoutSentMessage.visibility = View.VISIBLE
                    textViewSentMessage.text = message.text
                    textViewSentMessageTime.text = message.messageTimeFormatted
                } else {
                    constraintLayoutSentMessage.visibility = View.INVISIBLE
                    constraintLayoutReceivedMessage.visibility = View.VISIBLE
                    textViewReceivedMessage.text = message.text
                    textViewReceivedMessageTime.text = message.messageTimeFormatted
                }
            }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Message>() {

        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.messageTime == newItem.messageTime
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message) = false
    }

}
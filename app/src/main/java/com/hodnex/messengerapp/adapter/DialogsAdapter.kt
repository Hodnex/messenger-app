package com.hodnex.messengerapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hodnex.messengerapp.data.Dialog
import com.hodnex.messengerapp.databinding.ItemDialogBinding


class DialogsAdapter (private val listener: OnItemClickListener) : ListAdapter<Dialog, DialogsAdapter.DialogsViewHolder>(DialogsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogsViewHolder {
        val binding = ItemDialogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DialogsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DialogsViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class DialogsViewHolder(private val binding: ItemDialogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener{
                    val position = adapterPosition
                    if(position != RecyclerView.NO_POSITION) {
                        val user = getItem(position)
                        listener.onItemClick(user)
                    }
                }
            }
        }

        fun bind(dialog: Dialog) {
            binding.apply {
                textViewUserNameDialog.text = dialog.name
                textViewLastMessage.text = dialog.lastMessage
                textViewLastMessageTime.text = dialog.timeFormatted
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(dialog: Dialog)
    }

    class DialogsDiffCallback : DiffUtil.ItemCallback<Dialog>() {
        override fun areItemsTheSame(oldItem: Dialog, newItem: Dialog) = oldItem.uid == newItem.uid

        override fun areContentsTheSame(oldItem: Dialog, newItem: Dialog) = false
    }
}
package com.hodnex.messengerapp.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hodnex.messengerapp.R
import com.hodnex.messengerapp.adapter.ChatAdapter
import com.hodnex.messengerapp.databinding.FragmentChatBinding
import com.hodnex.messengerapp.viewmodel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val viewModel: ChatViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentChatBinding.bind(view)
        val chatAdapter = ChatAdapter(viewModel.currentId)

        binding.apply {
            recyclerViewChat.apply {
                adapter = chatAdapter
                layoutManager = LinearLayoutManager(requireContext()).apply {
                    reverseLayout = true
                }
                setHasFixedSize(true)
            }

            buttonSendMessage.setOnClickListener {
                if (editTextSendMessage.text.isNotEmpty()){
                    viewModel.sendMessage(editTextSendMessage.text.toString())
                    editTextSendMessage.setText("")
                }
            }
        }

        (requireActivity() as AppCompatActivity).supportActionBar?.customView?.findViewById<TextView>(
            R.id.tvTitle
        )?.text = viewModel.title

        viewModel.messages.observe(viewLifecycleOwner) {
            chatAdapter.submitList(it)
            binding.recyclerViewChat.smoothScrollToPosition(0)
        }
    }

}
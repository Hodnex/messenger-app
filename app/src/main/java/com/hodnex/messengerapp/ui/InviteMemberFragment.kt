package com.hodnex.messengerapp.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hodnex.messengerapp.R
import com.hodnex.messengerapp.databinding.FragmentInviteMemberBinding
import com.hodnex.messengerapp.util.exhaustive
import com.hodnex.messengerapp.viewmodel.InviteMemberViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class InviteMemberFragment : Fragment(R.layout.fragment_invite_member) {

    private val viewModel: InviteMemberViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentInviteMemberBinding.bind(view)

        binding.apply {
            buttonInvite.setOnClickListener {
                val email = editTextTextEmailAddress.text.toString()
                viewModel.inviteMemberClick(email)
            }
        }

        (requireActivity() as AppCompatActivity).supportActionBar?.customView?.findViewById<TextView>(
            R.id.tvTitle
        )?.text = "Invite Member"

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.inviteMemberEvent.collect { event ->
                when (event) {
                    is InviteMemberViewModel.InviteMemberEvent.NavigateBack -> {
                        setFragmentResult(
                            "invite_member_request",
                            bundleOf("invite_member_request" to event.msg)
                        )
                        findNavController().popBackStack()
                    }
                    is InviteMemberViewModel.InviteMemberEvent.ShowErrorMessage -> {
                        binding.editTextTextEmailAddress.clearFocus()
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                    }
                }.exhaustive
            }
        }
    }
}
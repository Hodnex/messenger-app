package com.hodnex.messengerapp.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.hodnex.messengerapp.R
import com.hodnex.messengerapp.adapter.DialogsAdapter
import com.hodnex.messengerapp.adapter.InvitationsAdapter
import com.hodnex.messengerapp.data.Dialog
import com.hodnex.messengerapp.data.Invitation
import com.hodnex.messengerapp.databinding.FragmentHomeBinding
import com.hodnex.messengerapp.util.exhaustive
import com.hodnex.messengerapp.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), DialogsAdapter.OnItemClickListener,
    InvitationsAdapter.OnItemClickListener {

    private val viewModel: HomeViewModel by viewModels()
    private val dialogsAdapter = DialogsAdapter(this)
    private val invitationsAdapter = InvitationsAdapter(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentHomeBinding.bind(view)

        binding.apply {
            recyclerViewDialogs.apply {
                adapter = dialogsAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            tabLayoutHome.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when (tab!!.text) {
                        "Dialogs" -> {
                            recyclerViewDialogs.adapter = dialogsAdapter
                        }
                        "Invitations" -> {
                            recyclerViewDialogs.adapter = invitationsAdapter
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

            })
        }

        setFragmentResultListener("invite_member_request") { _, bundle ->
            val result = bundle.getString("invite_member_request")
            Snackbar.make(requireView(), "$result", Snackbar.LENGTH_SHORT).show()
        }

        setupOptionsActionBar()
        setupObservers()
        setHasOptionsMenu(true)
    }

    private fun setupOptionsActionBar() {
        (requireActivity() as AppCompatActivity).supportActionBar?.show()

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            customView?.findViewById<TextView>(
                R.id.tvTitle
            )?.text = "Home"
            setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun setupObservers() {
        viewModel.dialogs.observe(viewLifecycleOwner) {
            dialogsAdapter.submitList(it)
        }

        viewModel.invitations.observe(viewLifecycleOwner) {
            invitationsAdapter.submitList(it)
        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.dialogsEvent.collect { event ->
                when (event) {
                    is HomeViewModel.HomeEvent.NavigateToChatScreen -> {
                        val action = HomeFragmentDirections.actionDialogsFragmentToChatFragment(
                            event.dialog,
                            event.dialog.name
                        )
                        findNavController().navigate(action)
                    }
                    is HomeViewModel.HomeEvent.ShowAddDialogMessage -> {
                        Snackbar.make(requireView(), "Dialog Added", Snackbar.LENGTH_SHORT).show()
                    }
                    is HomeViewModel.HomeEvent.ShowUndoDeleteInvitationMessage -> {
                        Snackbar.make(requireView(), "Invitation delete", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.undoDeleteInvitation(event.invitation)
                            }.show()
                    }
                    is HomeViewModel.HomeEvent.NavigateToInviteMemberScreen -> {
                        val action = HomeFragmentDirections.actionGlobalInviteMemberFragment()
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_new_invitation, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_invite_member) {
            viewModel.inviteMember()
        }
        return true
    }

    override fun onAcceptButtonClick(invitation: Invitation) {
        viewModel.addDialog(invitation)
    }

    override fun onDeclineButtonClick(invitation: Invitation) {
        viewModel.deleteInvitation(invitation)
    }

    override fun onItemClick(dialog: Dialog) {
        viewModel.onUserSelected(dialog)
    }
}

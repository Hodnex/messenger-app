package com.hodnex.messengerapp.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hodnex.messengerapp.R
import com.hodnex.messengerapp.databinding.FragmentEnterNameBinding
import com.hodnex.messengerapp.util.exhaustive
import com.hodnex.messengerapp.viewmodel.EnterNameViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class EnterNameFragment : Fragment(R.layout.fragment_enter_name) {

    private val viewModel: EnterNameViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentEnterNameBinding.bind(view)

        binding.apply {
            buttonEnterName.setOnClickListener {
                val username = editTextName.text.toString()
                viewModel.enterName(username)
            }
        }

        setupObservers()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.enterNameEvent.collect { event ->
                when(event) {
                    is EnterNameViewModel.EnterNameEvent.NavigateToHomeScreen -> {
                        val action = EnterNameFragmentDirections.actionEnterNameFragmentToDialogsFragment()
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }
    }
}
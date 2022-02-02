package com.hodnex.messengerapp.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hodnex.messengerapp.R
import com.hodnex.messengerapp.data.DataRepository
import com.hodnex.messengerapp.databinding.FragmentSignInBinding
import com.hodnex.messengerapp.viewmodel.SignInViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : Fragment(R.layout.fragment_sign_in) {

    private val viewModel: SignInViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val binding = FragmentSignInBinding.bind(view)


        binding.apply {
            buttonSignIn.setOnClickListener {
                val email = editTextEmail.text.toString()
                val password = editTextPassword.text.toString()
                if (email == "" || password == "") {
                    Snackbar.make(requireView(), "Fill all fields", Snackbar.LENGTH_SHORT).show()
                } else {
                    viewModel.signInClick(email, password)
                }
            }
        }


        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.signUp.observe(viewLifecycleOwner) {
            when (it) {
                is DataRepository.DataEvent.Failure -> {
                    Snackbar.make(requireView(), it.msg, Snackbar.LENGTH_LONG).show()
                }
                is DataRepository.DataEvent.SignInSuccess -> {
                    val action = SignInFragmentDirections.actionSignInFragmentToDialogsFragment()
                    findNavController().navigate(action)
                }
                is DataRepository.DataEvent.SignUpSuccess -> {
                    val action = SignInFragmentDirections.actionSignInFragmentToEnterNameFragment()
                    findNavController().navigate(action)
                }
            }
        }
    }
}
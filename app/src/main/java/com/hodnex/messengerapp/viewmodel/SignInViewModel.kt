package com.hodnex.messengerapp.viewmodel

import androidx.lifecycle.*
import com.hodnex.messengerapp.data.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    val authorizationEvent = repository.authorizationEvent.asFlow()

    fun signInClick(email: String, password: String) = viewModelScope.launch {
        val user = repository.findUserByEmail(email)
        if (user != null) {
            repository.signIn(email, password)
        } else {
            repository.signUp(email, password)
        }
    }
}
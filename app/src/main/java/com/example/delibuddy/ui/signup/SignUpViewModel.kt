package com.example.delibuddy.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class SignUpViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val email: LiveData<String> = savedStateHandle.getLiveData("email", "")

    private val _password = MutableLiveData("")
    val password: LiveData<String> = _password
    private val _passwordConfirm = MutableLiveData("")
    val passwordConfirm: LiveData<String> = _passwordConfirm

    fun setPassword(value: String) {
        _password.value = value
    }

    fun setPasswordConfirm(value: String) {
        _passwordConfirm.value = value
    }

    fun setEmail(value: String) {
        savedStateHandle["email"] = value
    }
}

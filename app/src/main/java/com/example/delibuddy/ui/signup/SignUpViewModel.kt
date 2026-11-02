package com.example.delibuddy.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class SignUpViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val email: LiveData<String> = savedStateHandle.getLiveData("email", "")

    fun setEmail(value: String) {
        savedStateHandle["email"] = value
    }
}

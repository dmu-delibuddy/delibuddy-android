package com.example.delibuddy.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.delibuddy.data.auth.AuthRepository

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _text = MutableLiveData("DELI-BUDDY")
    val text: LiveData<String> = _text

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    fun checkLoginStatus() {
        _isLoggedIn.value = authRepository.isLoggedIn()
    }
}

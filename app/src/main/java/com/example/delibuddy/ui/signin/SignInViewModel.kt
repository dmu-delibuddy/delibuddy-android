package com.example.delibuddy.ui.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignInViewModel : ViewModel() {

    private val _text = MutableLiveData("로그인")
    val text: LiveData<String> = _text
}

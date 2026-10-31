package com.example.delibuddy.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "한국어 테스트 하나 둘 삼 넷"
    }
    val text: LiveData<String> = _text
}
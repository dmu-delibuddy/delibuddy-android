package com.example.delibuddy.ui.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyPageViewModel : ViewModel() {

    private val _text = MutableLiveData("마이페이지")
    val text: LiveData<String> = _text
}

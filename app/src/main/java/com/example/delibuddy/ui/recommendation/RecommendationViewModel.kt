package com.example.delibuddy.ui.recommendation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecommendationViewModel : ViewModel() {

    private val _text = MutableLiveData("위치 추천")
    val text: LiveData<String> = _text
}

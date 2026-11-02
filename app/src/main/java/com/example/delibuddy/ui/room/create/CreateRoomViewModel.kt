package com.example.delibuddy.ui.room.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/*
    * 방 만들기 1 ~ 4 단계 상태.
    * 입력값도 앞으로 여기에 모아서 이전 으로 돌아가도 값이 유지되게 함.
*/

class CreateRoomViewModel : ViewModel() {

    private val _step = MutableLiveData(1)
    val step: LiveData<Int> = _step

    fun next() {
        val s = _step.value ?: 1
        if (s < LAST_STEP) _step.value = s + 1
    }

    fun prev() {
        val s = _step.value ?: 1
        if (s > 1) _step.value = s - 1
    }

    companion object {
        const val LAST_STEP = 4
    }
}
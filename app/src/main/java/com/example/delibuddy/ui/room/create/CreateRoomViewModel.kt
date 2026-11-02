package com.example.delibuddy.ui.room.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.delibuddy.data.room.RoomMode

/*
    * 방 만들기 1 ~ 4 단계 상태.
    * 입력값도 앞으로 여기에 모아서 이전 으로 돌아가도 값이 유지되게 함.
*/

class CreateRoomViewModel : ViewModel() {

    private val _step = MutableLiveData(1)
    val step: LiveData<Int> = _step
    // 1단계 : 방 정보
    private val _title = MutableLiveData("")
    val title : LiveData<String> = _title

    private val _maxParticipants = MutableLiveData(DEFAULT_PARTICIPANTS)
    val maxParticipants : LiveData<Int> = _maxParticipants

    private val _mode = MutableLiveData(RoomMode.PICKUP_ONLY)
    val mode : LiveData<RoomMode> = _mode

    fun setTitle(value: String) { _title.value = value }
    fun setMode(value: RoomMode) { _mode.value = value }

    fun increaseParticipants() {
        val n = _maxParticipants.value ?: DEFAULT_PARTICIPANTS
        if (n < MAX_PARTICIPANTS) _maxParticipants.value = n + 1
    }

    fun decreaseParticipants() {
        val n = _maxParticipants.value ?: DEFAULT_PARTICIPANTS
        if (n > MIN_PARTICIPANTS) _maxParticipants.value = n - 1
    }

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
        const val TITLE_MAX = 30
        const val MIN_PARTICIPANTS = 2 // 방장 포함
        const val MAX_PARTICIPANTS = 6
        const val DEFAULT_PARTICIPANTS = 3 // 기본값 설정
    }
}
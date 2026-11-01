package com.example.delibuddy.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.asLiveData
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach

class HomeViewModel(
    private val roomRepository: RoomRepository = RoomRepository()
) : ViewModel() {

    // 홈 방 목록 (firebase 실시간 구독)
    val rooms : LiveData<List<RoomModel>> = roomRepository.observeRooms()
        .onEach { Log.d(TAG, "rooms=${it.size}개") }
        .catch { e -> Log.e(TAG, "observrRooms 실패", e) }
        .asLiveData()

    companion object {
        private const val TAG = "HOMEVM"
    }
}

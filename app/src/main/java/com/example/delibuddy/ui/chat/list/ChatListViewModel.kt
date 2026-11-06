package com.example.delibuddy.ui.chat.list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf

/*
    * 하단 탭 "채팅" : 내가 방장 / 참여자인 방 목록
*/
class ChatListViewModel(
    private val roomRepository: RoomRepository = RoomRepository(),
) : ViewModel() {

    // 내 채팅방 목록 (로그인 안 했거나 실패하면 빈 목록)
    val rooms: LiveData<List<RoomModel>> =
        (FirebaseAuth.getInstance().currentUser?.uid
            ?.let { uid -> roomRepository.observeMyRooms(uid) }
            ?: flowOf(emptyList()))
            .catch { e ->
                Log.e(TAG, "내 채팅방 구독 실패", e)
                emit(emptyList())
            }
            .asLiveData()

    companion object {
        private const val TAG = "ChatListVM"
    }
}
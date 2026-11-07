package com.example.delibuddy.ui.room

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.delibuddy.data.room.FoodCategory
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomRepository
import com.example.delibuddy.data.room.RoomStatus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach

class RoomListViewModel(
    private val roomRepository: RoomRepository = RoomRepository()
) : ViewModel() {

    // ---- 필터 상태 (화면 버튼이 바꿈) ----
    private val mode = MutableStateFlow(RoomMode.PICKUP_ONLY)
    private val category = MutableStateFlow<FoodCategory?>(null)
    private val joinableOnly = MutableStateFlow(false)
    private val query = MutableStateFlow("")

    // 지금 로그인한 내 uid (내 방 판단용)
    private val myUid =
        FirebaseAuth.getInstance().currentUser?.uid

    // Firebase 에서 받은 전체 목록 (모집 중 + 정원 마감 + 모집 마감)
    private val allRooms = roomRepository.observeRooms()
        .onEach { Log.d(TAG, "rooms=${it.size}개") }
        .catch { e -> Log.e(TAG, "observeRooms 실패", e) }

    // 화면에 보여줄 목록
    val rooms : LiveData<List<RoomModel>> =
        combine(allRooms, mode, category, joinableOnly, query) { list, m, c, j, q ->
            list.filter { isVisible(it) }
                .filter { it.mode == m }
                .filter { c == null || it.category == c }
                .filter { !j || it.isJoinable }
                .filter {
                    q.isBlank() ||
                            it.title.contains(q, ignoreCase = true) ||
                            it.restaurant.contains(q, ignoreCase = true)
                }
        }.asLiveData()

    // 모집 중인 방은 모두에게,
    // 모집 마감 / 마감 시간 지난 방은 방장, 참여자에게만 보임
    private fun isVisible(room : RoomModel) : Boolean {
        val mine = room.isHost(myUid) ||
                room.isParticipant(myUid)
        val open = (room.status == RoomStatus.RECRUITING ||
                room.status == RoomStatus.FULL) &&
                !room.isDeadLinePassed
        return open || mine
    }

    // 화면에서 필터를 바꿀 때 부르는 함수
    // 모드 필터 변경
    fun setMode(value : RoomMode) { mode.value = value }
    // 카테고리 필터 변경 (null = 전체)
    fun setCategory(value : FoodCategory?) { category.value = value }
    // 참여 가능한 방만 보기 변경
    fun setJoinableOnly(value : Boolean) { joinableOnly.value = value }
    // 검색어 변경
    fun setQuery(value : String) { query.value = value }

    companion object {
        private const val TAG = "RoomListVM" // Logcat
    }
}

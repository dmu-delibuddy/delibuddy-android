package com.example.delibuddy.ui.room.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.delibuddy.data.room.FoodCategory
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine

class RoomListViewModel(
    private val roomRepository: RoomRepository = RoomRepository()
) : ViewModel() {

    // ---- 필터 상태 (화면 버튼이 바꿈) ----
    private val mode = MutableStateFlow(RoomMode.PICKUP_ONLY)
    private val category = MutableStateFlow<FoodCategory?>(null)
    private val query = MutableStateFlow("")

    // Firebase에서 받은 모집 중인 방
    private val recruitingRooms = roomRepository.observeRooms()
        .catch { }

    // 화면에 보여줄 목록
    val rooms: LiveData<List<RoomModel>> =
        combine(recruitingRooms, mode, category, query) { list, m, c, q ->
            list.filter { it.isJoinable }
                .filter { it.mode == m }
                .filter { c == null || it.category == c }
                .filter {
                    q.isBlank() ||
                            it.title.contains(q, ignoreCase = true) ||
                            it.restaurant.contains(q, ignoreCase = true)
                }
        }.asLiveData()

    // 화면에서 필터를 바꿀 때 부르는 함수
    // 모드 필터 변경
    fun setMode(value: RoomMode) {
        mode.value = value
    }

    // 카테고리 필터 변경 (null = 전체)
    fun setCategory(value: FoodCategory?) {
        category.value = value
    }

    // 검색어 변경
    fun setQuery(value: String) {
        query.value = value
    }
}

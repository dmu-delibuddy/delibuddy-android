package com.example.delibuddy.ui.room.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.data.chat.ChatRepository
import com.example.delibuddy.data.chat.SystemMessage
import com.example.delibuddy.data.room.RoomException
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomRepository
import com.example.delibuddy.data.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/*
    * 방 상세 화면. roomId 는 Navigation 인자로 받아 SavedStateHandle 에서 꺼냄
    * 방 문서를 실시간 구독해서 인원 / 상태 변화를 바로 반영
*/
class RoomDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val roomRepository = RoomRepository()

    // 참여 전에 송금 계좌가 등록돼 있는지 확인할 때 사용
    private val userRepository = UserRepository()

    val roomId: String = savedStateHandle[ARG_ROOM_ID] ?: ""

    // 화면 상태 : 방 있음 / 방 없음(삭제됨) / 실패. 첫 값 전까지는 화면에서 로딩 표시
    sealed interface DetailState {
        data class Loaded(val room: RoomModel) : DetailState
        data object NotFound : DetailState
        data object Error : DetailState
    }

    // 방 문서 실시간 구독 → 화면 상태로 변환 (구독 실패 시 Error)
    val state: LiveData<DetailState> = roomRepository.observeRoom(roomId)
        .map<RoomModel?, DetailState> { room ->
            room?.let { DetailState.Loaded(it) } ?: DetailState.NotFound
        }
        .catch { e ->
            Log.e(TAG, "observeRoom 실패: $roomId", e)
            emit(DetailState.Error)
        }
        .asLiveData()

    // 버튼 처리 상태
    sealed interface ActionState {
        data object Idle : ActionState
        data object Loading : ActionState
        data object Done : ActionState
        data class Failed (
            val reason : RoomException.Reason? // reason == null : 네트워크 등 알 수 없는 실패
        ) : ActionState
    }

    // 지금 로그인한 내 uid (방장 / 참여자 판단용)
    private val myUid =
        FirebaseAuth.getInstance().currentUser?.uid

    // 하단 버튼 처리 상태 (대기 / 처리 중 / 성공 / 실패)
    private val _actionState =
        MutableLiveData<ActionState>(ActionState.Idle)

    val actionState : LiveData<ActionState> =
        _actionState

    // 참여 성공 시 채팅방에 시스템 안내를 남긴다.
    private val chatRepository = ChatRepository()

    // 버튼 동작 실행 -> 성공 | 실패 상태 전달
    fun joinRoom() {
        if (_actionState.value == ActionState.Loading)
            return // 중복 클릭 방지
        _actionState.value = ActionState.Loading
        viewModelScope.launch {
            _actionState.value = try {
                requireAccount()
                roomRepository.joinRoom(roomId)
                notifyChat()
                ActionState.Done
            } catch (e : RoomException) {
                ActionState.Failed(e.reason)
            } catch (e : Exception) {
                Log.e(TAG, "참여 실패 : $roomId", e)
                ActionState.Failed(null)
            }
        }
    }

    // 송금 계좌(은행, 계좌번호)가 없으면 참여 막기
    // 회원가입에서 계좌 입력을 건너뛸 수 있어서 참여 직전에 확인
    private suspend fun requireAccount() {
        val uid = myUid ?: throw RoomException(RoomException.Reason.NOT_LOGGED_IN)
        val user = userRepository.getById(uid).await()
        if (user?.bankName.isNullOrBlank() || user?.account.isNullOrBlank()) {
            throw RoomException(RoomException.Reason.ACCOUNT_REQUIRED)
        }
    }

    // 채팅방에 시스템 안내 남기기. 실패해도 본 동작은 계속
    private suspend fun notifyChat() {
        try {
            chatRepository.sendSystem(roomId, SystemMessage.JOINED)
        } catch (e: Exception) {
            Log.w(TAG, "참여 시스템 메시지 실패", e)
        }
    }

    // 채팅방에 들어갈 수 있는지 (방장 / 참여자만)
    fun canChat(room : RoomModel) : Boolean =
        room.isHost(myUid) || room.isParticipant(myUid)

    // 토스트 보여준 뒤 대기 상태로
    fun clearActionState() {
        _actionState.value = ActionState.Idle
    }

    companion object {
        const val ARG_ROOM_ID = "roomId"
        private const val TAG = "RoomDetailVM"
    }
}

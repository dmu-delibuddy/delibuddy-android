package com.example.delibuddy.ui.room.detail

import com.example.delibuddy.data.room.RoomNotLoggedInException
import com.example.delibuddy.data.room.RoomAccountRequiredException
import android.content.ContentResolver
import android.net.Uri
import androidx.annotation.StringRes
import com.example.delibuddy.R
import com.example.delibuddy.ui.room.toRoomError
import com.example.delibuddy.data.image.ImageRepository
import com.example.delibuddy.data.order.OrderModel
import com.example.delibuddy.data.order.OrderRepository
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.data.chat.ChatRepository
import com.example.delibuddy.data.chat.SystemMessage
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomRepository
import com.example.delibuddy.data.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
        data class Error(@StringRes val message: Int) : DetailState
    }

    // 방 문서 실시간 구독 → 화면 상태로 변환 (구독 실패 시 Error)
    val state: LiveData<DetailState> = roomRepository.observeRoom(roomId)
        .map<RoomModel?, DetailState> { room ->
            room?.let { DetailState.Loaded(it) } ?: DetailState.NotFound
        }
        .catch { e ->
            Log.e(TAG, "observeRoom 실패: $roomId", e)
            emit(DetailState.Error(e.toRoomError(R.string.room_detail_load_error)))
        }
        .asLiveData()

    // 버튼 처리 상태
    sealed interface ActionState {
        data object Idle : ActionState
        data object Loading : ActionState
        data object Done : ActionState
        data class Failed(
            @StringRes val message: Int,
            val accountRequired: Boolean = false,
        ) : ActionState
    }

    // 지금 로그인한 내 uid (방장 / 참여자 판단용)
    private val myUid =
        FirebaseAuth.getInstance().currentUser?.uid

    // 하단 버튼 처리 상태 (대기 / 처리 중 / 성공 / 실패)
    private val _actionState =
        MutableLiveData<ActionState>(ActionState.Idle)

    val actionState: LiveData<ActionState> =
        _actionState

    // 참여 성공 시 채팅방에 시스템 안내를 남긴다.
    private val chatRepository = ChatRepository()

    // 버튼 동작 실행 -> 성공 | 실패 상태 전달
    fun joinRoom(price: Long, photo: Uri, memo: String?, resolver: ContentResolver) {
        require(price > 0)
        if (_actionState.value == ActionState.Loading)
            return // 중복 클릭 방지
        _actionState.value = ActionState.Loading
        viewModelScope.launch {
            _actionState.value = try {
                requireAccount()
                joinWithOrder(price, photo, memo, resolver)
                notifyChat()
                ActionState.Done
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "참여 실패 : $roomId", e)
                ActionState.Failed(
                    message = e.toRoomError(R.string.room_detail_action_error),
                    accountRequired = e is RoomAccountRequiredException,
                )
            }
        }
    }

    private suspend fun joinWithOrder(
        price: Long,
        photo: Uri,
        memo: String?,
        resolver: ContentResolver,
    ) {
        val callerContext = currentCoroutineContext()
        callerContext.ensureActive()
        // 업로드 결과를 받은 뒤 정리 여부를 결정할 때까지 취소로 흐름이 끊기지 않게 한다.
        withContext(NonCancellable) {
            val images = ImageRepository()
            val image = images.uploadImage(photo, resolver)
            try {
                // 업로드 중 화면이 닫혔다면 이미지만 정리하고 참여는 시작하지 않는다.
                callerContext.ensureActive()
                roomRepository.joinRoom(roomId)
                try {
                    OrderRepository().saveMyOrder(
                        roomId,
                        OrderModel(price = price, imageId = image.id, memo = memo),
                    )
                } catch (orderError: Exception) {
                    try {
                        roomRepository.leaveRoom(roomId)
                    } catch (rollbackError: Exception) {
                        orderError.addSuppressed(rollbackError)
                    }
                    throw orderError
                }
            } catch (error: Exception) {
                // 방 참여 또는 주문 저장이 실패한 경우, 예외 종류와 관계없이 정리한다.
                try {
                    images.deleteImage(image.id)
                } catch (cleanupError: Exception) {
                    error.addSuppressed(cleanupError)
                }
                throw error
            }
            // 주문 저장에 성공한 뒤 전달되는 취소는 위 catch 밖에서 처리되어 이미지를 유지한다.
        }
    }

    // 송금 계좌(은행, 계좌번호)가 없으면 참여 막기
    // 회원가입에서 계좌 입력을 건너뛸 수 있어서 참여 직전에 확인
    private suspend fun requireAccount() {
        val uid = myUid ?: throw RoomNotLoggedInException()
        val user = userRepository.getById(uid)
        if (user?.bankName.isNullOrBlank() || user?.account.isNullOrBlank()) {
            throw RoomAccountRequiredException()
        }
    }

    // 채팅방에 시스템 안내 남기기. 실패해도 본 동작은 계속
    private suspend fun notifyChat() {
        try {
            chatRepository.sendSystem(roomId, SystemMessage.JOINED)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "참여 시스템 메시지 실패", e)
        }
    }

    // 채팅방에 들어갈 수 있는지 (방장 / 참여자만)
    fun canChat(room: RoomModel): Boolean =
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

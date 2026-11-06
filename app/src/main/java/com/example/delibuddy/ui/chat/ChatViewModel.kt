package com.example.delibuddy.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.data.chat.ChatModel
import com.example.delibuddy.data.chat.ChatRepository
import com.example.delibuddy.data.chat.MessageType
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
    * 채팅방 화면
    * roomId 는 방 상세에서 Navigation 인자로 받음 (하단 탭으로 오면 null)
*/
class ChatViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val chatRepository = ChatRepository()
    private val roomRepository = RoomRepository()

    // 방 상세에서 넘어온 방 id. 하단 탭으로 오면 null
    val roomId: String? = savedStateHandle[ARG_ROOM_ID]

    // 화면 상태 : 방 없음(하단 탭) / 정상 / 불러오기 실패
    enum class ScreenState { NO_ROOM, READY, ERROR }

    // 아직 서버 저장이 끝나지 않은 내 메시지
    private data class PendingMessage(
        val id: String,
        val content: String,
        val failed: Boolean = false,
    )

    // 방 정보 실시간 구독 (방장 판단, 상단 제목)
    private val room: Flow<RoomModel?> =
        roomId?.let { id ->
            roomRepository.observeRoom(id)
                .catch { emit(null) }
        } ?: flowOf(null)

    // 메시지 구독 실패 여부 (참여자가 아니면 규칙에 막힘)
    private val loadFailed = MutableStateFlow(false)

    // 서버 메시지 실시간 구독
    private val serverMessages: Flow<List<ChatModel>> =
        roomId?.let { id ->
            chatRepository.observeMessages(id)
                .onEach { loadFailed.value = false }
                .catch { e ->
                    Log.e(TAG, "메시지 구독 실패: $id", e)
                    loadFailed.value = true
                    emit(emptyList())
                }
        } ?: flowOf(emptyList())

    // 보내는 중 / 실패한 내 메시지 (id → 메시지)
    private val pending =
        MutableStateFlow<Map<String, PendingMessage>>(emptyMap())

    // 상단에 보여줄 방 제목
    val roomTitle: LiveData<String> =
        room.map { it?.title.orEmpty() }.asLiveData()

    // 화면 상태
    val screenState: LiveData<ScreenState> =
        loadFailed.map { failed ->
            when {
                roomId == null -> ScreenState.NO_ROOM
                failed -> ScreenState.ERROR
                else -> ScreenState.READY
            }
        }.asLiveData()

    // 화면에 보여줄 메시지 목록
    val items: LiveData<List<ChatItem>> =
        combine(serverMessages, pending, room) { server, local, room ->
            buildItems(server, local, room)
        }.asLiveData()

    // 서버 메시지 뒤에, 서버에 아직 없는 내 메시지를 붙임
    private fun buildItems(
        server: List<ChatModel>,
        local: Map<String, PendingMessage>,
        room: RoomModel?,
    ): List<ChatItem> {
        val serverIds = server.map { it.id }.toSet()
        val fromServer = server.map { toItem(it, room) }
        val notSaved = local.values
            .filter { it.id !in serverIds }
            .map { toPendingItem(it) }
        return fromServer + notSaved
    }

    // 서버 메시지 → 화면용 메시지
    // createdAt 이 null 이면 아직 서버 저장 전(보내는 중)
    private fun toItem(
        message: ChatModel,
        room: RoomModel?,
    ): ChatItem =
        if (message.type == MessageType.SYSTEM) {
            ChatItem.System(
                id = message.id,
                content = message.content.orEmpty(),
            )
        } else {
            ChatItem.Bubble(
                id = message.id,
                isMine = message.senderId == chatRepository.myUid,
                isFromHost = message.senderId == room?.hostId,
                content = message.content,
                createdAt = message.createdAt,
                sendState =
                    if (message.createdAt == null) SendState.SENDING
                    else SendState.SENT,
                deleted = message.deletedAt != null,
            )
        }

    // 서버에 없는 내 메시지 → 화면용 메시지
    private fun toPendingItem(message: PendingMessage): ChatItem =
        ChatItem.Bubble(
            id = message.id,
            isMine = true,
            isFromHost = false,
            content = message.content,
            createdAt = null,
            sendState =
                if (message.failed) SendState.FAILED
                else SendState.SENDING,
            deleted = false,
        )

    // 메시지 보내기. 보낼 수 있으면 true (입력창 비우기용)
    fun send(content: String): Boolean {
        val id = roomId ?: return false
        val text = content.trim()
        if (text.isEmpty()) return false
        save(PendingMessage(chatRepository.newMessageId(id), text))
        return true
    }

    // 실패한 메시지를 같은 id 로 다시 저장 (중복 방지)
    fun retry(messageId: String) {
        val message = pending.value[messageId] ?: return
        if (message.failed) save(message)
    }

    // 보내는 중으로 표시 → 저장 → 성공이면 목록에서 빼고, 실패면 실패 표시
    private fun save(message: PendingMessage) {
        val id = roomId ?: return
        val sending = message.copy(failed = false)
        pending.update { it + (sending.id to sending) }
        viewModelScope.launch {
            try {
                chatRepository.sendText(id, sending.id, sending.content)
                pending.update { it - sending.id }
            } catch (e: Exception) {
                Log.e(TAG, "메시지 전송 실패: ${sending.id}", e)
                val failed = sending.copy(failed = true)
                pending.update { it + (failed.id to failed) }
            }
        }
    }

    companion object {
        const val ARG_ROOM_ID = "roomId"
        private const val TAG = "ChatVM"
    }
}
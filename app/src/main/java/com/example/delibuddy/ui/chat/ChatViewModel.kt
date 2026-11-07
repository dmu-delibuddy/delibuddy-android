package com.example.delibuddy.ui.chat

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.data.chat.Attachment
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
    * roomId 는 방 상세 / 내 채팅방 목록에서 Navigation 인자로 받음
    * 사진 업로드에 ContentResolver 가 필요해서 AndroidViewModel 사용
*/
class ChatViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val chatRepository = ChatRepository()
    private val roomRepository = RoomRepository()

    // 넘어온 방 id (없으면 null)
    val roomId: String? = savedStateHandle[ARG_ROOM_ID]

    // 화면 상태 : 방 없음 / 정상 / 불러오기 실패
    enum class ScreenState { NO_ROOM, READY, ERROR }

    // 아직 서버 저장이 끝나지 않은 내 메시지
    private data class PendingMessage(
        val id: String,
        val content: String? = null,
        val imageUri: Uri? = null,          // 보내려는 기기 사진
        val attachment: Attachment? = null, // 업로드가 끝난 첨부 (재전송 때 다시 올리지 않음)
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

    // 서버 메시지 실시간 구독. 사진이 있으면 다운로드 URL 요청
    private val serverMessages: Flow<List<ChatModel>> =
        roomId?.let { id ->
            chatRepository.observeMessages(id)
                .onEach { list ->
                    loadFailed.value = false
                    requestImageUrls(list)
                }
                .catch { e ->
                    Log.e(TAG, "메시지 구독 실패: $id", e)
                    loadFailed.value = true
                    emit(emptyList())
                }
        } ?: flowOf(emptyList())

    // 보내는 중 / 실패한 내 메시지 (id -> 메시지)
    private val pending =
        MutableStateFlow<Map<String, PendingMessage>>(emptyMap())

    // 받아 둔 사진 주소 (storagePath -> 다운로드 URL)
    private val imageUrls =
        MutableStateFlow<Map<String, String>>(emptyMap())

    // 이미 URL 을 요청한 storagePath (중복 요청 방지)
    private val requestedPaths = mutableSetOf<String>()

    // 내가 보낸 사진의 기기 주소 (URL 받기 전까지 이걸로 보여줌)
    private val localImages = mutableMapOf<String, Uri>()

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
        combine(serverMessages, pending, room, imageUrls) { server, local, room, urls ->
            buildItems(server, local, room, urls)
        }.asLiveData()

    // 서버 메시지 뒤에, 서버에 아직 없는 내 메시지를 붙임
    private fun buildItems(
        server: List<ChatModel>,
        local: Map<String, PendingMessage>,
        room: RoomModel?,
        urls: Map<String, String>,
    ): List<ChatItem> {
        val serverIds = server.map { it.id }.toSet()
        val fromServer = server.map { toItem(it, room, urls) }
        val notSaved = local.values
            .filter { it.id !in serverIds }
            .map { toPendingItem(it) }
        return fromServer + notSaved
    }

    // 서버 메시지 -> 화면용 메시지
    // createdAt 이 null 이면 아직 서버 저장 전(보내는 중)
    private fun toItem(
        message: ChatModel,
        room: RoomModel?,
        urls: Map<String, String>,
    ): ChatItem {
        if (message.type == MessageType.SYSTEM) {
            return ChatItem.System(
                id = message.id,
                content = message.content.orEmpty(),
            )
        }
        val path = message.attachments.firstOrNull()?.storagePath
        return ChatItem.Bubble(
            id = message.id,
            isMine = message.senderId == chatRepository.myUid,
            isFromHost = message.senderId == room?.hostId,
            content = message.content,
            createdAt = message.createdAt,
            sendState =
                if (message.createdAt == null) SendState.SENDING
                else SendState.SENT,
            deleted = message.deletedAt != null,
            hasImage = path != null,
            imageUrl = path?.let { urls[it] }
                ?: localImages[message.id]?.toString(),
        )
    }

    // 서버에 없는 내 메시지 -> 화면용 메시지
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
            hasImage = message.imageUri != null,
            imageUrl = message.imageUri?.toString(),
        )

    // 사진이 붙은 메시지마다 다운로드 URL 을 한 번씩 받아 둠
    private fun requestImageUrls(messages: List<ChatModel>) {
        messages
            .flatMap { it.attachments }
            .map { it.storagePath }
            .filter { it.isNotBlank() && requestedPaths.add(it) }
            .forEach { path ->
                viewModelScope.launch {
                    try {
                        val url = chatRepository.imageUrl(path)
                        imageUrls.update { it + (path to url) }
                    } catch (e: Exception) {
                        Log.w(TAG, "사진 주소 받기 실패: $path", e)
                        requestedPaths.remove(path) // 다음 목록 갱신 때 다시 시도
                    }
                }
            }
    }

    // 텍스트 보내기. 보낼 수 있으면 true (입력창 비우기용)
    fun send(content: String): Boolean {
        val id = roomId ?: return false
        val text = content.trim()
        if (text.isEmpty()) return false
        save(PendingMessage(chatRepository.newMessageId(id), content = text))
        return true
    }

    // 사진 보내기 (갤러리에서 고른 사진)
    fun sendImage(uri: Uri) {
        val id = roomId ?: return
        val messageId = chatRepository.newMessageId(id)
        localImages[messageId] = uri
        save(PendingMessage(messageId, imageUri = uri))
    }

    // 실패한 메시지를 같은 id 로 다시 저장 (중복 방지)
    fun retry(messageId: String) {
        val message = pending.value[messageId] ?: return
        if (message.failed) save(message)
    }

    // 보내는 중으로 표시 -> 저장 -> 성공이면 목록에서 빼고, 실패면 실패 표시
    private fun save(message: PendingMessage) {
        val id = roomId ?: return
        val sending = message.copy(failed = false)
        putPending(sending)
        viewModelScope.launch {
            try {
                if (sending.imageUri != null) {
                    sendImageMessage(id, sending, sending.imageUri)
                } else {
                    chatRepository.sendText(id, sending.id, sending.content.orEmpty())
                }
                pending.update { it - sending.id }
            } catch (e: Exception) {
                Log.e(TAG, "메시지 전송 실패: ${sending.id}", e)
                // 업로드까지 끝났다면 첨부 정보가 담긴 최신 값을 실패로 남김
                val latest = pending.value[sending.id] ?: sending
                putPending(latest.copy(failed = true))
            }
        }
    }

    // 사진 : 처음이면 업로드 -> 첨부 기억 -> 메시지 저장
    private suspend fun sendImageMessage(
        roomId: String,
        message: PendingMessage,
        uri: Uri,
    ) {
        val attachment = message.attachment
            ?: chatRepository.uploadImage(uri, getApplication<Application>().contentResolver)
                .also { putPending(message.copy(attachment = it)) }
        chatRepository.sendImage(roomId, message.id, attachment)
    }

    // 보내는 중 목록에 넣거나 갱신
    private fun putPending(message: PendingMessage) {
        pending.update { it + (message.id to message) }
    }

    companion object {
        const val ARG_ROOM_ID = "roomId"
        private const val TAG = "ChatVM"
    }
}

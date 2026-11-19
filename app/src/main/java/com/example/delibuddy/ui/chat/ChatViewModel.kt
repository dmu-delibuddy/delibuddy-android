package com.example.delibuddy.ui.chat

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.data.chat.ChatModel
import com.example.delibuddy.data.chat.ChatRepository
import com.example.delibuddy.data.chat.MessageType
import com.example.delibuddy.data.image.ImageRepository
import com.example.delibuddy.data.profile.ProfileModel
import com.example.delibuddy.data.profile.ProfileRepository
import com.example.delibuddy.data.profile.ProfileType
import com.example.delibuddy.data.room.RoomException
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import com.example.delibuddy.R
import com.example.delibuddy.data.room.RoomStatus
import com.example.delibuddy.data.chat.SystemMessage

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
    private val imageRepository = ImageRepository()
    private val roomRepository = RoomRepository()
    private val profileRepository = ProfileRepository()

    // 넘어온 방 id (없으면 null)
    val roomId: String? = savedStateHandle[ARG_ROOM_ID]

    // 화면 상태 : 방 없음 / 정상 / 불러오기 실패
    enum class ScreenState { NO_ROOM, READY, ERROR }

    sealed interface LeaveRoomState {
        data object Idle : LeaveRoomState
        data object Loading : LeaveRoomState
        data object Done : LeaveRoomState
        data class Failed(val reason: RoomException.Reason?) : LeaveRoomState
    }

    private val _leaveRoomState = MutableLiveData<LeaveRoomState>(LeaveRoomState.Idle)
    val leaveRoomState: LiveData<LeaveRoomState> = _leaveRoomState

    fun leaveRoom() {
        if (_leaveRoomState.value != LeaveRoomState.Idle) return
        val id = roomId
        if (id == null) {
            _leaveRoomState.value = LeaveRoomState.Failed(RoomException.Reason.NOT_FOUND)
            return
        }

        _leaveRoomState.value = LeaveRoomState.Loading
        viewModelScope.launch {
            _leaveRoomState.value = try {
                roomRepository.leaveRoom(id)
                LeaveRoomState.Done
            } catch (e: CancellationException) {
                throw e
            } catch (e: RoomException) {
                LeaveRoomState.Failed(e.reason)
            } catch (e: Exception) {
                Log.e(TAG, "방 나가기 실패: $id", e)
                LeaveRoomState.Failed(null)
            }
        }
    }

    fun clearLeaveRoomFailure() {
        if (_leaveRoomState.value is LeaveRoomState.Failed) {
            _leaveRoomState.value = LeaveRoomState.Idle
        }
    }

    // 아직 서버 저장이 끝나지 않은 내 메시지
    private data class PendingMessage(
        val id: String,
        val content: String? = null,
        val imageUri: Uri? = null,          // 보내려는 기기 사진
        val imageId: String? = null,        // 업로드한 이미지 ID (재전송 때 재사용)
        val failed: Boolean = false,
    )

    // 방 정보 실시간 구독 (방장 판단, 상단 제목)
    private val room: Flow<RoomModel?> =
        roomId?.let { id ->
            roomRepository.observeRoom(id)
                .catch { emit(null) }
        } ?: flowOf(null)
    private val sharedRoom =
        room.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

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

    // 받아 둔 사진 주소 (이미지 ID -> 다운로드 URL)
    private val imageUrls =
        MutableStateFlow<Map<String, String>>(emptyMap())

    // 이미 URL 을 요청한 이미지 ID (중복 요청 방지)
    private val requestedImageIds = mutableSetOf<String>()
    private val profiles =
        MutableStateFlow<Map<Pair<String, ProfileType>, ProfileModel>>(emptyMap())
    private val requestedProfiles = mutableSetOf<Pair<String, ProfileType>>()

    // 내가 보낸 사진의 기기 주소 (URL 받기 전까지 이걸로 보여줌)
    private val localImages = mutableMapOf<String, Uri>()

    val roomHeader: LiveData<RoomModel?> = sharedRoom.asLiveData()

    private val _statusError = MutableLiveData<String?>()
    val statusError : LiveData<String?> = _statusError

    private var changingStatus = false

    fun isHost(room : RoomModel) : Boolean = room.isHost(chatRepository.myUid)

    fun advanceStatus() {
        val id = roomId ?: return
        val status = roomHeader.value?.status ?: return
        val next = status.next ?: return
        if (changingStatus)
            return
        changingStatus = true
        viewModelScope.launch {
            try {
                roomRepository.advanceStatus(id, status)
                SystemMessage.forStatus(next)?.let {
                    chatRepository.sendSystem(id, it)
                }
            } catch (e : CancellationException) {
                throw e
            } catch (e : RoomException) {
                _statusError.value = e.reason.message
            } catch (e : Exception) {
                Log.e(TAG, "방 상태 변경 실패 : $id", e)
                _statusError.value = getApplication<Application>().getString(R.string.notice_action_failed)
            } finally {
                changingStatus = false
            }
        }
    }

    fun clearStatusError() {
        _statusError.value = null
    }

    val participants: LiveData<List<ChatParticipant>> =
        combine(sharedRoom, profiles) { currentRoom, cachedProfiles ->
            if (currentRoom == null) return@combine emptyList()
            val type = profileType(currentRoom.mode)
            val ids = currentRoom.participantIds.filter { it.isNotBlank() }.distinct()
            requestProfileIds(ids, type)
            ids.map { id ->
                val profile = cachedProfiles[id to type]
                ChatParticipant(
                    id,
                    profile?.nickname.orEmpty(),
                    profile?.imageUrl,
                    id == currentRoom.hostId
                )
            }
        }.asLiveData()

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
        combine(
            serverMessages,
            pending,
            sharedRoom,
            imageUrls,
            profiles
        ) { server, local, room, urls, profiles ->
            if (room != null) requestProfiles(server, room.mode)
            buildItems(server, local, room, urls, profiles)
        }.asLiveData()

    // 서버 메시지 뒤에, 서버에 아직 없는 내 메시지를 붙임
    private fun buildItems(
        server: List<ChatModel>,
        local: Map<String, PendingMessage>,
        room: RoomModel?,
        urls: Map<String, String>,
        profiles: Map<Pair<String, ProfileType>, ProfileModel>,
    ): List<ChatItem> {
        val serverIds = server.map { it.id }.toSet()
        val fromServer = server.map { toItem(it, room, urls, profiles) }
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
        profiles: Map<Pair<String, ProfileType>, ProfileModel>,
    ): ChatItem {
        if (message.type == MessageType.SYSTEM) {
            return ChatItem.System(
                id = message.id,
                content = message.content.orEmpty(),
            )
        }
        val imageId = message.imageId?.takeIf { it.isNotBlank() }
        val profile = room?.let { currentRoom ->
            message.senderId?.let { profiles[it to profileType(currentRoom.mode)] }
        }
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
            hasImage = imageId != null,
            imageUrl = imageId?.let { urls[it] }
                ?: localImages[message.id]?.toString(),
            senderNickname = profile?.nickname?.takeIf { it.isNotBlank() },
            senderImageUrl = profile?.imageUrl?.takeIf { it.isNotBlank() },
        )
    }

    private fun profileType(mode: RoomMode): ProfileType = when (mode) {
        RoomMode.PICKUP_ONLY -> ProfileType.PICKUP_ONLY
        RoomMode.MEAL_BUDDY -> ProfileType.MEAL_BUDDY
    }

    private fun requestProfiles(messages: List<ChatModel>, mode: RoomMode) {
        val ids = messages.asSequence()
            .filter { it.type == MessageType.NORMAL }
            .mapNotNull { it.senderId?.takeIf { id -> id.isNotBlank() && id != chatRepository.myUid } }
            .distinct()
            .toList()
        requestProfileIds(ids, profileType(mode))
    }

    private fun requestProfileIds(ids: List<String>, type: ProfileType) {
        ids.asSequence().map { it to type }
            .filter { requestedProfiles.add(it) }
            .forEach { key ->
                viewModelScope.launch {
                    try {
                        profileRepository.getById(key.first, key.second)?.let { profile ->
                            profiles.update { it + (key to profile) }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.w(TAG, "프로필 조회 실패: ${key.first}", e)
                    }
                }
            }
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
            .mapNotNull { it.imageId?.takeIf(String::isNotBlank) }
            .filter { requestedImageIds.add(it) }
            .forEach { imageId ->
                viewModelScope.launch {
                    try {
                        val image = imageRepository.getImage(imageId)
                            ?: throw IllegalStateException("이미지를 찾을 수 없습니다: $imageId")
                        imageUrls.update { it + (imageId to image.url) }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.w(TAG, "사진 주소 받기 실패: $imageId", e)
                        requestedImageIds.remove(imageId) // 다음 목록 갱신 때 다시 시도
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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "메시지 전송 실패: ${sending.id}", e)
                // 업로드까지 끝났다면 이미지 ID가 담긴 최신 값을 실패로 남김
                val latest = pending.value[sending.id] ?: sending
                putPending(latest.copy(failed = true))
            }
        }
    }

    // 사진 : 처음이면 업로드 -> 이미지 ID 기억 -> 메시지 저장
    private suspend fun sendImageMessage(
        roomId: String,
        message: PendingMessage,
        uri: Uri,
    ) {
        val imageId = message.imageId
            ?: imageRepository.uploadImage(uri, getApplication<Application>().contentResolver).id
                .also { putPending(message.copy(imageId = it)) }
        chatRepository.sendImage(roomId, message.id, imageId)
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

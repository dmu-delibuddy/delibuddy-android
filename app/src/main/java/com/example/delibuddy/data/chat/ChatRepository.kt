package com.example.delibuddy.data.chat

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.example.delibuddy.data.image.ImageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/*
    * 채팅 메시지 저장 / 실시간 구독, 채팅 이미지 업로드
    * 위치 : rooms/{roomId}/messages/{messageId}
*/
class ChatRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val imageRepository: ImageRepository = ImageRepository(),
) {
    // 로그인한 내 uid
    val myUid: String?
        get() = auth.currentUser?.uid

    // 방의 메시지 컬렉션
    private fun messages(roomId: String) =
        db.collection("rooms").document(roomId)
            .collection("messages")

    // 보내기 전에 메시지 id 를 미리 만듦
    // 실패 후 재전송할 때 같은 id 로 다시 저장해서 중복 방지
    fun newMessageId(roomId: String): String =
        messages(roomId).document().id

    // 방 메시지 실시간 구독 (오래된 순, 최근 MESSAGE_LIMIT 개)
    fun observeMessages(roomId: String): Flow<List<ChatModel>> = callbackFlow {
        val registration = messages(roomId)
            .orderBy("createdAt")
            .limitToLast(MESSAGE_LIMIT)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents.orEmpty()
                    .mapNotNull { toMessage(it) }
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    // 문서 -> 메시지. 형식이 잘못된 문서는 로그만 남기고 건너뜀
    private fun toMessage(doc: DocumentSnapshot): ChatModel? = try {
        doc.toObject(ChatModel::class.java)
    } catch (e: RuntimeException) {
        Log.w(TAG, "메시지 변환 실패: ${doc.id}", e)
        null
    }

    // 일반 메시지 전송 (텍스트)
    suspend fun sendText(roomId: String, messageId: String, content: String) {
        val uid = myUid ?: throw IllegalStateException("로그인이 필요합니다.")
        messages(roomId).document(messageId).set(
            ChatModel(
                senderId = uid,
                type = MessageType.NORMAL,
                content = content,
            )
        ).await()
    }

    // 일반 메시지 전송 (사진 한 장, 내용 없음)
    suspend fun sendImage(roomId: String, messageId: String, attachment: Attachment) {
        val uid = myUid ?: throw IllegalStateException("로그인이 필요합니다.")
        messages(roomId).document(messageId).set(
            ChatModel(
                senderId = uid,
                type = MessageType.NORMAL,
                content = null,
                attachments = listOf(attachment),
            )
        ).await()
    }

    // 시스템 메시지 전송 (참여, 마감 등 안내). 보낸 사람 없음
    suspend fun sendSystem(roomId: String, content: String) {
        messages(roomId).document().set(
            ChatModel(
                senderId = null,
                type = MessageType.SYSTEM,
                content = content,
            )
        ).await()
    }

    // 기기 사진을 Storage 에 올리고 첨부 정보로 돌려줌 (ImageRepository 사용)
    suspend fun uploadImage(uri: Uri, contentResolver: ContentResolver): Attachment {
        val image = imageRepository.uploadImage(uri, contentResolver)
        return Attachment(
            storagePath = image.storagePath,
            contentType = image.contentType,
            width = image.width,
            height = image.height,
        )
    }

    // Storage 경로 -> 화면에 띄울 다운로드 URL
    suspend fun imageUrl(storagePath: String): String =
        storage.reference.child(storagePath).downloadUrl.await().toString()

    companion object {
        private const val TAG = "ChatRepo"
        private const val MESSAGE_LIMIT = 200L
    }
}

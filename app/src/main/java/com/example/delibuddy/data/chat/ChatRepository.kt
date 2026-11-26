package com.example.delibuddy.data.chat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val myUid: String
        get() = auth.currentUser?.uid ?: throw ChatNotLoggedInException()

    private fun messages(roomId: String) =
        db.collection("rooms").document(roomId).collection("messages")

    fun newMessageId(roomId: String): String = messages(roomId).document().id

    fun observeMessages(roomId: String): Flow<List<ChatModel>> = callbackFlow {
        val registration = messages(roomId).orderBy("createdAt").limitToLast(MESSAGE_LIMIT)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents.orEmpty().mapNotNull { toMessage(it) }
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    private fun toMessage(doc: DocumentSnapshot): ChatModel? =
        try {
            doc.toObject(ChatModel::class.java)
        } catch (e: RuntimeException) {
            null
        }


    suspend fun sendText(
        roomId: String,
        messageId: String,
        content: String,
        sharedAccount: SharedAccount? = null,
    ) {
        val uid = myUid
        messages(roomId).document(messageId).set(
            ChatModel(
                senderId = uid,
                type = MessageType.NORMAL,
                content = content,
                sharedAccount = sharedAccount,
            )
        ).await()
    }

    suspend fun sendImage(roomId: String, messageId: String, imageId: String) {
        val uid = myUid
        messages(roomId).document(messageId).set(
            ChatModel(
                senderId = uid,
                type = MessageType.NORMAL,
                content = null,
                imageId = imageId,
            )
        ).await()
    }

    suspend fun sendSystem(roomId: String, content: String) {
        messages(roomId).document().set(
            ChatModel(
                senderId = null,
                type = MessageType.SYSTEM,
                content = content,
            )
        ).await()
    }

    companion object {
        private const val MESSAGE_LIMIT = 200L
    }
}

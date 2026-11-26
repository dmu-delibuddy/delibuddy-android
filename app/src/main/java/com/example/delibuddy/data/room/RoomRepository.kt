package com.example.delibuddy.data.room

import android.util.Log
import com.example.delibuddy.data.chat.ChatModel
import com.example.delibuddy.data.chat.MessageType
import com.example.delibuddy.data.chat.SystemMessage
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await


class RoomRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val rooms = db.collection("rooms")

    val myUid: String get() = auth.currentUser?.uid ?: throw RoomNotLoggedInException()

    suspend fun createRoom(room: RoomModel) {
        val uid = myUid
        val ref = rooms.document()
        val newRoom = room.copy(
            hostId = uid,
            participantIds = listOf(uid),
            status = RoomStatus.RECRUITING,
            createdAt = Timestamp.now(),
        )
        db.runBatch { batch ->
            batch.set(ref, newRoom)
            batch.set(
                ref.collection("messages").document(),
                createSystemMessage(SystemMessage.ROOM_CREATED)
            )
        }.await()
    }

    suspend fun getRoom(roomId: String): RoomModel? =
        rooms.document(roomId).get().await().toObject(RoomModel::class.java)

    fun observeRoom(roomId: String): Flow<RoomModel?> = callbackFlow {
        val registration = rooms.document(roomId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(RoomModel::class.java))
        }
        awaitClose { registration.remove() }
    }

    fun observeRooms(
        mode: RoomMode? = null,
        category: FoodCategory? = null,
    ): Flow<List<RoomModel>> = callbackFlow {
        var query: Query = rooms.whereEqualTo("status", RoomStatus.RECRUITING.name)
        if (mode != null) query = query.whereEqualTo("mode", mode.name)
        if (category != null) query = query.whereEqualTo("category", category.name)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toObject(RoomModel::class.java)
                }.sortedByDescending { it.createdAt?.seconds ?: 0L }
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    fun observeMyRooms(uid: String): Flow<List<RoomModel>> = callbackFlow {
        val registration =
            rooms.whereArrayContains("participantIds", uid).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list =
                    snapshot?.documents.orEmpty().mapNotNull { it.toObject(RoomModel::class.java) }
                        .sortedWith(compareBy<RoomModel> {
                            it.status == RoomStatus.COMPLETED
                        }.thenByDescending {
                            it.createdAt?.seconds ?: 0L
                        })
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    suspend fun joinRoom(roomId: String) {
        val uid = myUid
        val ref = rooms.document(roomId)
        val messageRef = ref.collection("messages").document()
        db.runTransaction { tx ->
            val room = tx.get(ref).toObject(RoomModel::class.java) ?: throw RoomNotFoundException()
            when {
                room.isParticipant(uid) -> throw RoomAlreadyJoinedException()

                room.status != RoomStatus.RECRUITING -> throw RoomClosedException()

                room.isFull -> throw RoomFullException()
            }
            tx.update(ref, "participantIds", FieldValue.arrayUnion(uid))
            tx.set(messageRef, createSystemMessage(SystemMessage.JOINED))
        }.await()
    }

    suspend fun leaveRoom(roomId: String) {
        val uid = myUid
        val ref = rooms.document(roomId)
        val messageRef = ref.collection("messages").document()
        db.runTransaction { tx ->
            val room = tx.get(ref).toObject(RoomModel::class.java) ?: throw RoomNotFoundException()
            if (!room.isParticipant(uid)) return@runTransaction
            if (room.status != RoomStatus.RECRUITING && room.status != RoomStatus.COMPLETED) throw RoomCannotLeaveInProgressException()

            val remaining = room.participantIds.filterNot { it == uid }
            if (remaining.isEmpty()) {
                tx.delete(ref)
            } else {
                val updates = mutableMapOf<String, Any>("participantIds" to remaining)
                if (room.isHost(uid)) updates["hostId"] = remaining.first()
                tx.update(ref, updates)
                tx.set(messageRef, createSystemMessage(SystemMessage.LEFT))
            }
        }.await()
    }

    suspend fun advanceStatus(roomId: String, from: RoomStatus) {
        val to = from.next ?: throw RoomInvalidStateException()
        changeStatusAsHost(roomId, setOf(from), to)
    }

    suspend fun kickParticipant(roomId: String, participantId: String) {
        val uid = myUid
        val ref = rooms.document(roomId)
        val messageRef = ref.collection("messages").document()
        db.runTransaction { tx ->
            val room = tx.get(ref).toObject(RoomModel::class.java) ?: throw RoomNotFoundException()
            if (!room.isHost(uid)) throw RoomNotHostException()
            if (participantId == uid || !room.isParticipant(participantId)) {
                throw RoomInvalidStateException()
            }
            if (room.status != RoomStatus.RECRUITING && room.status != RoomStatus.COMPLETED) {
                throw RoomCannotLeaveInProgressException()
            }
            tx.update(ref, "participantIds", FieldValue.arrayRemove(participantId))
            tx.set(messageRef, createSystemMessage(SystemMessage.REMOVED))
        }.await()
    }

    private suspend fun changeStatusAsHost(roomId: String, from: Set<RoomStatus>, to: RoomStatus) {
        val uid = myUid
        val ref = rooms.document(roomId)
        val messageRef = ref.collection("messages").document()
        db.runTransaction { tx ->
            val room = tx.get(ref).toObject(RoomModel::class.java) ?: throw RoomNotFoundException()
            if (!room.isHost(uid)) throw RoomNotHostException()
            if (room.status !in from) throw RoomInvalidStateException()
            tx.update(ref, "status", to.name)
            SystemMessage.forStatus(to)?.let { content ->
                tx.set(messageRef, createSystemMessage(content))
            }
        }.await()
    }

    private fun createSystemMessage(content: String) = ChatModel(
        senderId = null,
        type = MessageType.SYSTEM,
        content = content,
    )
}

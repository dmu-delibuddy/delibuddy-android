package com.example.delibuddy.data.room

import android.util.Log
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

    // 로그인한 내 uid. 로그인 안 했으면 NOT_LOGGED_IN 예외
    private fun requireUid(): String =
        auth.currentUser?.uid ?: throw RoomNotLoggedInException()

    // -------------------- 방 생성 ---------------------
    suspend fun createRoom(room: RoomModel): String {
        val uid = requireUid()
        val ref = rooms.document()
        val newRoom = room.copy(
            hostId = uid,
            participantIds = listOf(uid),
            status = RoomStatus.RECRUITING,
            createdAt = Timestamp.now(),
        )
        ref.set(newRoom).await()
        return ref.id
    }

    // ----------------- 방 리스트 / 상세 ---------------------
    // 홈 화면에서 모집 중인 방을 실시간으로 구독한다.
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
            // 문서를 하나씩 변환. 형식이 잘못된 문서는 로그만 남기고 건너뜀 (앱 사망 방지)
            val list = snapshot?.documents.orEmpty()
                .mapNotNull { doc ->
                    try {
                        doc.toObject(RoomModel::class.java)
                    } catch (e: RuntimeException) {
                        Log.w("RoomRepo", "방 문서 변환 실패: ${doc.id}", e)
                        null
                    }
                }
                .sortedByDescending { it.createdAt?.seconds ?: 0L }
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    // 방 상세 한 번 조회
    suspend fun getRoom(roomId: String): RoomModel? =
        rooms.document(roomId).get().await().toObject(RoomModel::class.java)

    // 방 상세 실시간 구독 (참여 인원, 상태 변화 바로 반영)
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

    // ------------------- 방 상태관리 ---------------------------

    // 방 참여. 트랜잭션으로 정원 / 중복 / 모집 상태를 검사해서 동시에 눌러도 정원을 넘지 않음.
    suspend fun joinRoom(roomId: String) {
        val uid = requireUid()
        val ref = rooms.document(roomId)
        db.runTransaction { tx ->
            val room = tx.get(ref).toObject(RoomModel::class.java)
                ?: throw RoomNotFoundException()
            when {
                room.isParticipant(uid) ->
                    throw RoomAlreadyJoinedException()
                room.status != RoomStatus.RECRUITING ->
                    throw RoomClosedException()
                room.isFull ->
                    throw RoomFullException()
            }
            tx.update(ref, "participantIds", FieldValue.arrayUnion(uid))
        }.await()
    }

    // 모집 중이거나 완료된 방에서 나가고, 방장이 나가면 남은 첫 참여자에게 승계한다.
    suspend fun leaveRoom(roomId: String) {
        val uid = requireUid()
        val ref = rooms.document(roomId)
        db.runTransaction { tx ->
            val room = tx.get(ref).toObject(RoomModel::class.java)
                ?: throw RoomNotFoundException()
            if (!room.isParticipant(uid))
                return@runTransaction
            if (room.status != RoomStatus.RECRUITING && room.status != RoomStatus.COMPLETED)
                throw RoomCannotLeaveInProgressException()

            val remaining = room.participantIds.filterNot { it == uid }
            if (remaining.isEmpty()) {
                tx.delete(ref)
            } else {
                val updates = mutableMapOf<String, Any>("participantIds" to remaining)
                if (room.isHost(uid)) updates["hostId"] = remaining.first()
                tx.update(ref, updates)
            }
        }.await()
    }

    suspend fun removeParticipant(roomId: String, participantId: String) {
        val uid = requireUid()
        val ref = rooms.document(roomId)
        db.runTransaction { tx ->
            val room = tx.get(ref).toObject(RoomModel::class.java)
                ?: throw RoomNotFoundException()
            if (!room.isHost(uid)) throw RoomNotHostException()
            if (participantId == uid || !room.isParticipant(participantId)) {
                throw RoomInvalidStateException()
            }
            if (room.status != RoomStatus.RECRUITING && room.status != RoomStatus.COMPLETED) {
                throw RoomCannotLeaveInProgressException()
            }
            tx.update(ref, "participantIds", FieldValue.arrayRemove(participantId))
        }.await()
    }

    suspend fun advanceStatus (roomId : String, from : RoomStatus) {
        val to = from.next ?: throw RoomInvalidStateException()
        changeStatusAsHost(roomId, setOf(from), to)
    }

    // 방장만, 허용된 상태(from)일 때만 방 상태를 to 로 변경 (트랜잭션)
    private suspend fun changeStatusAsHost(roomId: String, from: Set<RoomStatus>, to: RoomStatus) {
        val uid = requireUid()
        val ref = rooms.document(roomId)
        db.runTransaction { tx ->
            val room = tx.get(ref).toObject(RoomModel::class.java)
                ?: throw RoomNotFoundException()
            if (!room.isHost(uid)) throw RoomNotHostException()
            if (room.status !in from) throw RoomInvalidStateException()
            tx.update(ref, "status", to.name)
        }.await()
    }

    fun observeMyRooms(uid: String): Flow<List<RoomModel>> = callbackFlow {
        val registration = rooms
            .whereArrayContains("participantIds", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents.orEmpty()
                    .mapNotNull { it.toObject(RoomModel::class.java) }
                    .sortedWith (
                        compareBy<RoomModel> {
                            it.status == RoomStatus.COMPLETED
                        }.thenByDescending {
                            it.createdAt?.seconds ?: 0L
                        }
                    )
                trySend(list)
            }
        awaitClose { registration.remove() }
    }
}

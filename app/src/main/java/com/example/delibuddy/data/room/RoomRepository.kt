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
import kotlin.Long.Companion.MAX_VALUE


class RoomRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    private val rooms = db.collection("rooms")

    private fun requireUid(): String =
        auth.currentUser?.uid ?: throw RoomException(RoomException.Reason.NOT_LOGGED_IN)

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
    /*
        * 홈 화면 방 목록 실시간 구독.
        * onlyJoinable = true 이면 모집 중 (RECRUITING)인 방만, false 이면 정원 마감도 함께 보여줌
    */

    fun observeRooms(
        onlyJoinable: Boolean = false,
        mode: RoomMode? = null,
        category: FoodCategory? = null,
    ): Flow<List<RoomModel>> = callbackFlow {
        var query: Query = if (onlyJoinable) {
            rooms.whereEqualTo("status", RoomStatus.RECRUITING.name)
        } else {
            rooms.whereIn("status", listOf(RoomStatus.RECRUITING.name, RoomStatus.FULL.name))
        }
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
                .filterNot { it.isDeadLinePassed }
                .sortedBy { it.deadline?.seconds ?: MAX_VALUE }
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

    // 방 참여. 트랜잭션으로 정원 / 중복 / 마감을 검사해서 동시에 눌러도 정원을 넘지 않음.
    suspend fun joinRoom(roomId: String) {
        val uid = requireUid()
        val ref = rooms.document(roomId)
        db.runTransaction { tx ->
            val room = tx.get(ref).toObject(RoomModel::class.java)
                ?: throw RoomException(RoomException.Reason.NOT_FOUND)
            when {
                room.isParticipant(uid) -> throw RoomException(RoomException.Reason.ALREADY_JOINED)
                room.status == RoomStatus.FULL || room.currentCount >= room.maxParticipants ->
                    throw RoomException(RoomException.Reason.FULL)

                room.status != RoomStatus.RECRUITING || room.isDeadLinePassed ->
                    throw RoomException(RoomException.Reason.CLOSED)
            }
            val nextStatus =
                if (room.currentCount + 1 >= room.maxParticipants) RoomStatus.FULL else RoomStatus.RECRUITING
            tx.update(
                ref, mapOf(
                    "participantIds" to FieldValue.arrayUnion(uid),
                    "status" to nextStatus.name,
                )
            )
        }.await()
    }

    // 참여 취소. 정원 마감 상태였으면 다시 모집 중으로 돌림
    suspend fun leaveRoom(roomId: String) {
        val uid = requireUid()
        val ref = rooms.document(roomId)
        db.runTransaction { tx ->
            val room = tx.get(ref).toObject(RoomModel::class.java)
                ?: throw RoomException(RoomException.Reason.NOT_FOUND)
            if (room.isHost(uid)) throw RoomException(RoomException.Reason.HOST_CANNOT_LEAVE)
            if (!room.isParticipant(uid)) return@runTransaction
            if (room.status != RoomStatus.RECRUITING && room.status != RoomStatus.FULL)
                throw RoomException(RoomException.Reason.CLOSED)   // 모집 마감 후에는 취소 불가
            tx.update(
                ref, mapOf(
                    "participantIds" to FieldValue.arrayRemove(uid),
                    "status" to RoomStatus.RECRUITING.name,
                )
            )
        }.await()
    }

    // 방장이 모집 마감: 모집 중이거나 정원 마감일 때만
    suspend fun closeRecruiting(roomId: String) = changeStatusAsHost(
        roomId,
        from = setOf(RoomStatus.RECRUITING, RoomStatus.FULL),
        to = RoomStatus.ORDER_CONFIRMED,
    )

    // 방장이 공동 주문 완료: 모집 마감(주문 확정) 상태일 때만
    suspend fun completeRoom(roomId: String) = changeStatusAsHost(
        roomId,
        from = setOf(RoomStatus.ORDER_CONFIRMED),
        to = RoomStatus.COMPLETED,
    )

    private suspend fun changeStatusAsHost(roomId: String, from: Set<RoomStatus>, to: RoomStatus) {
        val uid = requireUid()
        val ref = rooms.document(roomId)
        db.runTransaction { tx ->
            val room = tx.get(ref).toObject(RoomModel::class.java)
                ?: throw RoomException(RoomException.Reason.NOT_FOUND)
            if (!room.isHost(uid)) throw RoomException(RoomException.Reason.NOT_HOST)
            if (room.status !in from) throw RoomException(RoomException.Reason.INVALID_STATE)
            tx.update(ref, "status", to.name)
        }.await()
    }
}
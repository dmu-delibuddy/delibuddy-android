package com.example.delibuddy.data.order

import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    private fun orders(roomId: String) = db.collection("rooms").document(roomId)
        .collection("orders")

    suspend fun saveMyOrder(roomId: String, order: OrderModel) {
        require(order.price >= 0) { "주문 금액은 0 이상이어야 합니다." }
        changeMyOrder(roomId, order)
    }

    suspend fun deleteMyOrder(roomId: String) {
        changeMyOrder(roomId, null)
    }

    private suspend fun changeMyOrder(roomId: String, order: OrderModel?) {
        require(roomId.isNotBlank()) { "방 ID가 필요합니다." }
        val uid = checkNotNull(auth.currentUser?.uid) { "로그인이 필요합니다." }
        val roomRef = db.collection("rooms").document(roomId)
        val orderRef = orders(roomId).document(uid)
        db.runTransaction { transaction ->
            val room = transaction.get(roomRef).toObject(RoomModel::class.java)
            checkNotNull(room) { "방을 찾을 수 없습니다." }
            check(room.isParticipant(uid)) { "방 참여자만 주문을 변경할 수 있습니다." }
            check(room.status in setOf(RoomStatus.RECRUITING, RoomStatus.FULL)
                && !room.isDeadLinePassed) { "모집 마감 후에는 주문을 변경할 수 없습니다." }
            if (order == null) {
                transaction.delete(orderRef)
            } else {
                transaction.set(orderRef, order.copy(userId = uid))
            }
        }.await()
    }

    suspend fun getOrder(roomId: String, userId: String): OrderModel? {
        require(roomId.isNotBlank() && userId.isNotBlank()) { "방 ID와 사용자 ID가 필요합니다." }
        return orders(roomId).document(userId).get().await().toObject(OrderModel::class.java)
    }

    fun observeOrders(roomId: String): Flow<List<OrderModel>> = callbackFlow {
        require(roomId.isNotBlank()) { "방 ID가 필요합니다." }
        val registration = orders(roomId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            try {
                trySend(snapshot?.toObjects(OrderModel::class.java).orEmpty())
            } catch (error: Exception) {
                close(error)
            }
        }
        awaitClose { registration.remove() }
    }
}

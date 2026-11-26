package com.example.delibuddy.data.order

import com.example.delibuddy.data.image.ImageRepository
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class OrderRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val imageRepository = ImageRepository()

    private fun orders(roomId: String) = db.collection("rooms").document(roomId)
        .collection("orders")

    suspend fun saveMyOrder(roomId: String, order: OrderModel) {
        if (roomId.isBlank()) throw InvalidOrderRoomIdException()
        if (order.price < 0) throw InvalidOrderPriceException()
        val uid = auth.currentUser?.uid ?: throw OrderNotLoggedInException()

        val roomRef = db.collection("rooms").document(roomId)
        val orderRef = orders(roomId).document(uid)

        db.runTransaction { transaction ->
            val room = transaction.get(roomRef).toObject(RoomModel::class.java)
                ?: throw OrderRoomNotFoundException()
            if (!room.isParticipant(uid)) throw OrderNotParticipantException()
            if (room.status != RoomStatus.RECRUITING) throw OrderRoomClosedException()

            transaction.set(orderRef, order.copy(userId = uid))
        }.await()
    }

    suspend fun getOrder(roomId: String, userId: String): OrderModel? {
        if (roomId.isBlank()) throw InvalidOrderRoomIdException()
        if (userId.isBlank()) throw InvalidOrderUserIdException()
        return orders(roomId).document(userId).get().await().toObject(OrderModel::class.java)?.withImageUrl()
    }

    fun observeOrders(roomId: String): Flow<List<OrderModel>> = callbackFlow {
        if (roomId.isBlank()) throw InvalidOrderRoomIdException()
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
    }.map { orders -> orders.map { it.withImageUrl() } }

    private suspend fun OrderModel.withImageUrl(): OrderModel {
        val id = imageId?.takeIf { it.isNotBlank() } ?: return this
        return copy(imageUrl = imageRepository.getImage(id)?.url)
    }
}
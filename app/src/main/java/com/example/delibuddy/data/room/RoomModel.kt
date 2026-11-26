package com.example.delibuddy.data.room

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint

data class RoomModel(
    @DocumentId val id: String = "",
    val title: String = "",
    val hostId: String = "",
    val maxParticipants: Int = 2,
    val participantIds: List<String> = emptyList(),
    val mode: RoomMode = RoomMode.PICKUP_ONLY,
    val category: FoodCategory = FoodCategory.ETC,
    val restaurant: String = "",
    val deliveryApp: DeliveryApp = DeliveryApp.OTHER,
    val deliveryAppName: String = "",
    val location: GeoPoint? = null,
    val locationName: String = "",
    val minOrderAmount: Int = 0,
    val estimatedDeliveryFee: Int = 0,
    val foodCostSplitMethod: FoodCostSplitMethod = FoodCostSplitMethod.EQUAL_SPLIT,
    val deliveryFeeSplitMethod: DeliveryFeeSplitMethod = DeliveryFeeSplitMethod.EQUAL_SPLIT,
    val status: RoomStatus = RoomStatus.RECRUITING,
    val createdAt: Timestamp? = null,
) {
    @get:Exclude
    val currentCount: Int get() = participantIds.size

    @get:Exclude
    val deliveryFeePerPerson: Int get() = estimatedDeliveryFee / maxOf(currentCount, 1)

    @get:Exclude
    val isFull : Boolean
        get() = currentCount >= maxParticipants

    @get:Exclude
    val isJoinable: Boolean
        get() = status == RoomStatus.RECRUITING && !isFull

    fun isHost(uid: String?): Boolean = uid != null && uid == hostId

    fun isParticipant(uid: String?): Boolean = uid != null && uid in participantIds
}
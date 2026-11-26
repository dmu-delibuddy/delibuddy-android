package com.example.delibuddy.ui.chat.order

import com.example.delibuddy.data.order.OrderModel
import com.example.delibuddy.data.room.RoomModel

data class OrderSheet(
    val room: RoomModel,
    val rows: List<OrderSheetRow>,
    val foodTotal: Long,
    val deliveryFee: Long,
) {
    val submittedCount: Int get() = rows.count { it.order != null }
    val totalPayment: Long get() = foodTotal + deliveryFee
}

data class OrderSheetRow(
    val userId: String,
    val nickname: String,
    val isHost: Boolean,
    val order: OrderModel?,
    val foodShare: Long?,
    val deliveryShare: Long?,
) {
    val amount: Long?
        get() = if (foodShare != null && deliveryShare != null) foodShare + deliveryShare else null
}

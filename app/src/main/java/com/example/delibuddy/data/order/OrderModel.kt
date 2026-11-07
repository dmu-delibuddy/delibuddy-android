package com.example.delibuddy.data.order

import com.google.firebase.firestore.DocumentId

// rooms/{roomId}/orders/{userId}: 참여자당 주문 문서 하나
data class OrderModel(
    @DocumentId val userId: String = "",
    val price: Long = 0L,
    val cartScreenshotUrl: String? = null,
    val memo: String? = null,
)

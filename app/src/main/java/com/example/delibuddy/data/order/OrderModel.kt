package com.example.delibuddy.data.order

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class OrderModel(
    @DocumentId val userId: String = "",
    val price: Long = 0L,
    val imageId: String? = null,
    val memo: String? = null,
    @get:Exclude val imageUrl: String? = null,
)

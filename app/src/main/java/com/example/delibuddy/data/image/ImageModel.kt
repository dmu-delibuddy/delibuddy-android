package com.example.delibuddy.data.image

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ImageModel(
    @DocumentId val id: String = "",
    val url: String = "",
    val storagePath: String = "",
    val fileName: String = "",
    val contentType: String = "",
    val sizeBytes: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val createdAt: Timestamp? = null,
)

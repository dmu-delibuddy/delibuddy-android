package com.example.delibuddy.data.image

import com.google.firebase.Timestamp

data class ImageModel(
    val id: String = "",
    val url: String = "",
    val storagePath: String = "",
    val fileName: String = "",
    val contentType: String = "",
    val sizeBytes: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val createdAt: Timestamp? = null,
)

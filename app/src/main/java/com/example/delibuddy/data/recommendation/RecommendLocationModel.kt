package com.example.delibuddy.data.recommendation

import com.example.delibuddy.data.room.RoomMode
import com.google.firebase.firestore.Exclude

data class RecommendLocationModel(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val imagePath: String = "",
    val mode: RoomMode = RoomMode.PICKUP_ONLY,
    @get:Exclude val imageUrl: String = "",
)

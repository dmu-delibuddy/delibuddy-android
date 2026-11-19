package com.example.delibuddy.data.recommendation

import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.data.image.ImageRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class RecommendLocationRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val imageRepository: ImageRepository = ImageRepository(),
) {
    private val locations = db.collection("recommend-locations")

    suspend fun getLocations(mode: RoomMode? = null): List<RecommendLocation> {
        val query = if (mode == null) locations else locations.whereEqualTo("mode", mode.name)
        val results = query.get().await().toObjects(RecommendLocation::class.java)
        return coroutineScope {
            results.map { location ->
                async {
                    location.copy(imageUrl = imageRepository.getImageUrl(location.imagePath))
                }
            }.awaitAll()
        }
    }
}

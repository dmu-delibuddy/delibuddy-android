package com.example.delibuddy.data.recommendation

import com.example.delibuddy.data.image.ImageRepository
import com.example.delibuddy.data.room.RoomMode
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.json.JSONArray

class RecommendLocationRepository {
    private val imageRepository: ImageRepository = ImageRepository()
    private val assets = FirebaseApp.getInstance().applicationContext.assets

    suspend fun getLocations(): List<RecommendLocationModel> = withContext(Dispatchers.IO) {
        loadLocations().map { location ->
            async {
                if (location.imagePath.isBlank()) return@async location

                try {
                    location.copy(imageUrl = imageRepository.getImageUrl(location.imagePath))
                } catch (error: StorageException) {
                    location
                }
            }
        }.awaitAll()
    }

    suspend fun getPlaces(): List<RecommendLocationModel> =
        withContext(Dispatchers.IO) { loadLocations() }

    suspend fun getPlace(id: String): RecommendLocationModel? =
        getPlaces().firstOrNull { it.id == id }

    private fun loadLocations(): List<RecommendLocationModel> {
        return try {
            val jsonString = assets.open("recommend-locations.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<RecommendLocationModel>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val modeStr = obj.optString("mode", "PICKUP_ONLY")
                val roomMode = try {
                    RoomMode.valueOf(modeStr)
                } catch (_: Exception) {
                    RoomMode.PICKUP_ONLY
                }

                list.add(
                    RecommendLocationModel(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        description = obj.optString("description"),
                        lat = obj.optDouble("lat"),
                        lon = obj.optDouble("lon"),
                        imagePath = obj.optString("imagePath"),
                        mode = roomMode,
                    ),
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }
}
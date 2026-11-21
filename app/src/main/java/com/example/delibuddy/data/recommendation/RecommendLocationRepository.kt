package com.example.delibuddy.data.recommendation

import android.content.res.AssetManager
import android.util.Log
import com.example.delibuddy.data.image.ImageRepository
import com.example.delibuddy.data.room.RoomMode
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.json.JSONArray

class RecommendLocationRepository(
    private val imageRepository: ImageRepository = ImageRepository(),
) {
    suspend fun getLocations(assets: AssetManager): List<RecommendLocation> = coroutineScope {
        loadLocations(assets).map { location ->
            async {
                if (location.imagePath.isBlank()) return@async location

                try {
                    location.copy(imageUrl = imageRepository.getImageUrl(location.imagePath))
                } catch (error: StorageException) {
                    Log.w("RecommendLocationRepository", "Could not resolve ${location.imagePath}", error)
                    location
                }
            }
        }.awaitAll()
    }

    private fun loadLocations(assets: AssetManager): List<RecommendLocation> {
        return try {
            val jsonString = assets.open("recommend-locations.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<RecommendLocation>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val modeStr = obj.optString("mode", "PICKUP_ONLY")
                val roomMode = try {
                    RoomMode.valueOf(modeStr)
                } catch (_: Exception) {
                    RoomMode.PICKUP_ONLY
                }

                list.add(
                    RecommendLocation(
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

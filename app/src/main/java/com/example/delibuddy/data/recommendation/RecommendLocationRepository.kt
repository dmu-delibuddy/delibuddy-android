package com.example.delibuddy.data.recommendation

import android.content.res.AssetManager
import android.util.Log
import com.example.delibuddy.data.image.ImageRepository
import com.example.delibuddy.data.room.RoomMode
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray

class RecommendLocationRepository {
    private val imageRepository: ImageRepository = ImageRepository()

    suspend fun getLocations(assets: AssetManager): List<RecommendLocationModel> = coroutineScope {
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

    // 방 만들기 장소 선택용 : 사진 주소 없이 이름 · 좌표만 (네트워크 요청 없음)
    suspend fun getPlaces(assets: AssetManager): List<RecommendLocationModel> =
        withContext(Dispatchers.IO) { loadLocations(assets) }

    private fun loadLocations(assets: AssetManager): List<RecommendLocationModel> {
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

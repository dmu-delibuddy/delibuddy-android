package com.example.delibuddy.data.recommendation

import android.util.Log
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

    // 앱 내장 JSON 을 읽을 assets : Firebase 가 앱 시작 때 들고 있는 앱 Context 에서 꺼냄
    private val assets = FirebaseApp.getInstance().applicationContext.assets

    suspend fun getLocations(): List<RecommendLocationModel> = withContext(Dispatchers.IO) {
        loadLocations().map { location ->
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
    suspend fun getPlaces(): List<RecommendLocationModel> =
        withContext(Dispatchers.IO) { loadLocations() }

    // 지도에서 넘어온 장소 하나 : id 로 찾기 (없으면 null)
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

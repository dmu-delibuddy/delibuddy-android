package com.example.delibuddy.ui.recommendation

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.data.recommendation.RecommendLocation
import com.example.delibuddy.data.recommendation.RecommendLocationRepository
import com.example.delibuddy.data.room.RoomMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class RecommendationViewModel(
    private val repository: RecommendLocationRepository = RecommendLocationRepository(),
) : ViewModel() {

    private val _locations = MutableLiveData<List<RecommendLocation>>(emptyList())
    val locations: LiveData<List<RecommendLocation>> = _locations

    fun loadLocations(context: Context) {
        viewModelScope.launch {
            try {
                val firestoreLocations = withContext(Dispatchers.IO) {
                    repository.getLocations()
                }
                if (firestoreLocations.isNotEmpty()) {
                    _locations.value = firestoreLocations
                    return@launch
                }
            } catch (_: Exception) {

            }

            val localLocations = withContext(Dispatchers.IO) {
                loadFromAssets(context)
            }
            _locations.value = localLocations
        }
    }

    private fun loadFromAssets(context: Context): List<RecommendLocation> {
        return try {
            val jsonString = context.assets.open("recommend-locations.json").bufferedReader().use { it.readText() }
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

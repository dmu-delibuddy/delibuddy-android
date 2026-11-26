package com.example.delibuddy.ui.recommendation

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.data.recommendation.RecommendLocationModel
import com.example.delibuddy.data.recommendation.RecommendLocationRepository
import com.example.delibuddy.ui.room.create.CreateRoomViewModel
import kotlinx.coroutines.launch

class RecommendationViewModel(
    private val repository: RecommendLocationRepository = RecommendLocationRepository(),
) : ViewModel() {

    private val _locations = MutableLiveData<List<RecommendLocationModel>>(emptyList())
    val locations: LiveData<List<RecommendLocationModel>> = _locations

    private val _createRoomArgs = MutableLiveData<Bundle?>()
    val createRoomArgs: LiveData<Bundle?> = _createRoomArgs

    fun loadLocations(context: Context) {
        viewModelScope.launch {
            _locations.value =
                repository.getLocations(context.assets)
        }
    }

    fun createRoomAt(location: RecommendLocationModel) {
        _createRoomArgs.value = bundleOf(
            CreateRoomViewModel.ARG_PLACE_NAME to location.name,
            CreateRoomViewModel.ARG_PLACE_LAT to location.lat,
            CreateRoomViewModel.ARG_PLACE_LON to location.lon,
        )
    }

    fun onCreateRoomNavigated() {
        _createRoomArgs.value = null
    }
}

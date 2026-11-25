package com.example.delibuddy.ui.recommendation

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.data.recommendation.RecommendLocationModel
import com.example.delibuddy.data.recommendation.RecommendLocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecommendationViewModel(
    private val repository: RecommendLocationRepository = RecommendLocationRepository(),
) : ViewModel() {

    private val _locations = MutableLiveData<List<RecommendLocationModel>>(emptyList())
    val locations: LiveData<List<RecommendLocationModel>> = _locations

    fun loadLocations(context: Context) {
        viewModelScope.launch {
            _locations.value = withContext(Dispatchers.IO) {
                repository.getLocations(context.assets)
            }
        }
    }
}

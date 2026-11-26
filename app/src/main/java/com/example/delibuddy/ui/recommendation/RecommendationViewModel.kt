package com.example.delibuddy.ui.recommendation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.data.recommendation.RecommendLocationModel
import com.example.delibuddy.data.recommendation.RecommendLocationRepository
import kotlinx.coroutines.launch

class RecommendationViewModel(
    private val repository: RecommendLocationRepository = RecommendLocationRepository(),
) : ViewModel() {

    private val _locations = MutableLiveData<List<RecommendLocationModel>>(emptyList())
    val locations: LiveData<List<RecommendLocationModel>> = _locations

    fun loadLocations() {
        viewModelScope.launch {
            _locations.value = repository.getLocations()
        }
    }
}

package com.example.delibuddy.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.delibuddy.data.profile.ProfileModel
import com.example.delibuddy.data.profile.ProfileRepository
import com.example.delibuddy.data.profile.ProfileType

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: ProfileModel) : ProfileUiState()
    object NotFound : ProfileUiState()   // 프로필을 만들지 않은 사용자
    object Error : ProfileUiState()      // 네트워크 등 조회 실패
}

class ProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()

    private val _state = MutableLiveData<ProfileUiState>(ProfileUiState.Loading)
    val state: LiveData<ProfileUiState> = _state

    fun load(userId: String, type: ProfileType) {
        // 화면 회전 등으로 다시 불려도 이미 불러왔으면 다시 조회하지 않음
        if (_state.value is ProfileUiState.Success) return

        _state.value = ProfileUiState.Loading
        repository.getById(userId, type)
            .addOnSuccessListener { profile ->
                _state.value =
                    if (profile == null) ProfileUiState.NotFound else ProfileUiState.Success(profile)
            }
            .addOnFailureListener {
                _state.value = ProfileUiState.Error
            }
    }
}

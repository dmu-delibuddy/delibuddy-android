package com.example.delibuddy.ui.common.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.data.profile.ProfileModel
import com.example.delibuddy.data.profile.ProfileRepository
import com.example.delibuddy.data.profile.ProfileType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

// 프로필 모달 화면 상태
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: ProfileModel) : ProfileUiState()
    object NotFound : ProfileUiState()
    object Error : ProfileUiState()
}

/*
    * 프로필 모달(ProfileDialogFragment)용 ViewModel
    * - 선택한 사용자의 모드별 프로필을 불러온다.
*/
class ProfileViewModel : ViewModel() {

    private val profileRepository = ProfileRepository()

    private val _state = MutableLiveData<ProfileUiState>(ProfileUiState.Loading)
    val state: LiveData<ProfileUiState> = _state

    // 화면 회전으로 onViewCreated 가 다시 불려도 한 번만 불러오기
    private var started = false

    fun load(userId: String, type: ProfileType) {
        if (started) return
        started = true
        if (userId.isBlank()) {
            _state.value = ProfileUiState.NotFound
            return
        }
        _state.value = ProfileUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                val profile = profileRepository.getProfile(userId, type)
                if (profile == null) ProfileUiState.NotFound else ProfileUiState.Success(profile)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ProfileUiState.Error
            }
        }
    }
}

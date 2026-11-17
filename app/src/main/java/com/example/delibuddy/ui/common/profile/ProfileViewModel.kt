package com.example.delibuddy.ui.common.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.data.profile.ProfileModel
import com.example.delibuddy.data.profile.ProfileRepository
import com.example.delibuddy.data.profile.ProfileType
import com.example.delibuddy.data.user.UserRepository
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
    * - users/{uid} 문서가 없으면 없는 사용자(NotFound) : UserRepository
    * - 그 모드의 프로필 문서가 없으면 빈 프로필로 보여줌 (마이페이지와 같은 규칙) : ProfileRepository
*/
class ProfileViewModel : ViewModel() {

    private val profileRepository = ProfileRepository()
    private val userRepository = UserRepository()

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
                val user = userRepository.getById(userId)
                if (user == null) {
                    ProfileUiState.NotFound
                } else {
                    val profile = profileRepository.getById(userId, type)
                    ProfileUiState.Success(profile ?: ProfileModel(type = type))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "프로필 불러오기 실패: $userId / $type", e)
                ProfileUiState.Error
            }
        }
    }

    companion object {
        private const val TAG = "ProfileModalVM"
    }
}

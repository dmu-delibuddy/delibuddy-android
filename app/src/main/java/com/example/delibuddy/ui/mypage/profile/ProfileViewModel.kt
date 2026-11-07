package com.example.delibuddy.ui.mypage.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.R
import com.example.delibuddy.data.image.ImageRepository
import com.example.delibuddy.data.profile.ProfileModel
import com.example.delibuddy.data.profile.ProfileRepository
import com.example.delibuddy.data.profile.ProfileType
import com.example.delibuddy.data.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/*
    * 마이페이지 프로필 도메인 (프로필 카드 + 프로필 수정 창이 함께 씀)
    * - 프로필은 모드별로 따로 있음 : users/{uid}/profiles/{PICKUP_ONLY | MEAL_BUDDY}
    *   (회원가입 때 두 개 모두 랜덤 닉네임으로 만들어짐)
    * - 모드 버튼은 "어느 모드 프로필을 볼지" 고르는 것. 고른 모드의 프로필을 실시간 구독
    * - 생년월일 : 화면은 프로필 카드지만 저장 위치는 users/{uid}
    * 사진 업로드에 ContentResolver 가 필요해서 AndroidViewModel 사용
*/
class ProfileViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val profileRepository = ProfileRepository()
    private val userRepository = UserRepository()
    private val imageRepository = ImageRepository()

    // 로그인한 내 uid
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid

    // 화면에 띄울 안내 문구 (토스트로 보여준 뒤 null 로 비움)
    private val _message = MutableLiveData<Int?>()
    val message: LiveData<Int?> = _message

    // 저장 중인지 (중복 저장 방지)
    private val _saving = MutableLiveData(false)
    val saving: LiveData<Boolean> = _saving

    // 지금 보고 있는 모드 (처음엔 픽업만)
    private val _selectedType = MutableStateFlow(ProfileType.PICKUP_ONLY)
    val selectedType: LiveData<ProfileType> = _selectedType.asLiveData()

    // 고른 모드의 프로필 실시간 구독 (모드를 바꾸면 그 모드 프로필로 바꿔 구독)
    // 문서가 없으면 그 모드의 빈 프로필
    @OptIn(ExperimentalCoroutinesApi::class)
    val profile: LiveData<ProfileModel> =
        _selectedType
            .flatMapLatest { type -> observeProfile(type) }
            .asLiveData()

    // 한 모드의 프로필 구독. 실패하면 안내 문구 + 빈 프로필
    private fun observeProfile(type: ProfileType) =
        (userId?.let { profileRepository.observe(it, type) } ?: flowOf(null))
            .map { it ?: ProfileModel(type = type) }
            .catch { e ->
                Log.e(TAG, "프로필 구독 실패: $type", e)
                _message.value = R.string.mypage_load_error
                emit(ProfileModel(type = type))
            }

    // 내 생년월일 (yyyy-MM-dd, 없으면 null)
    private val _birthDate = MutableLiveData<String?>()
    val birthDate: LiveData<String?> = _birthDate

    init {
        loadBirthDate()
    }

    // users/{uid} 에서 생년월일 한 번 불러오기
    private fun loadBirthDate() {
        val id = userId ?: return
        viewModelScope.launch {
            try {
                _birthDate.value = userRepository.getById(id).await()
                    ?.birthDate
                    ?.takeIf { it.isNotBlank() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "생년월일 불러오기 실패", e)
                _message.value = R.string.mypage_load_error
            }
        }
    }

    // 볼 모드 고르기 (픽업만 / 밥친구). 저장하지 않고 화면만 바뀜
    fun selectMode(type: ProfileType) {
        if (_selectedType.value != type) _selectedType.value = type
    }

    // 입력 검사. 문제 있으면 안내 문구, 없으면 null
    @StringRes
    fun validate(nickname: String): Int? =
        if (nickname.isBlank()) R.string.mypage_nickname_required else null

    // 지금 고른 모드의 프로필 저장
    // 새 사진이 있으면 먼저 업로드, removeImage 면 기본 사진으로
    // 생년월일이 바뀌었으면 users 문서의 생년월일만 수정
    fun save(
        nickname: String,
        bio: String,
        birthDate: String?,
        newImage: Uri?,
        removeImage: Boolean,
    ) {
        val id = userId ?: return
        val type = _selectedType.value
        val current = profile.value?.takeIf { it.type == type }
            ?: ProfileModel(type = type)
        launchSave(R.string.mypage_profile_saved) {
            val imageUrl = when {
                newImage != null -> uploadImage(newImage)
                removeImage -> ""
                else -> current.imageUrl.orEmpty()
            }
            profileRepository.save(
                id,
                current.copy(
                    type = type,
                    nickname = nickname.trim(),
                    bio = bio.trim(),
                    imageUrl = imageUrl,
                ),
            )
            if (birthDate != _birthDate.value) {
                userRepository.updateBirthDate(id, birthDate).await()
                _birthDate.value = birthDate
            }
        }
    }

    // 사진을 Storage 에 올리고 화면에 띄울 URL 을 돌려줌
    private suspend fun uploadImage(uri: Uri): String =
        imageRepository.uploadImage(
            uri,
            getApplication<Application>().contentResolver,
        ).url

    // 저장 공통 처리 : 저장 중 표시 -> 실행 -> 성공 / 실패 문구
    private fun launchSave(
        @StringRes successMessage: Int?,
        block: suspend () -> Unit,
    ) {
        if (_saving.value == true) return
        _saving.value = true
        viewModelScope.launch {
            try {
                block()
                successMessage?.let { _message.value = it }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "프로필 저장 실패", e)
                _message.value = R.string.mypage_save_error
            } finally {
                _saving.value = false
            }
        }
    }

    // 토스트를 보여준 뒤 비우기 (화면 회전 시 중복 방지)
    fun clearMessage() {
        _message.value = null
    }

    companion object {
        private const val TAG = "ProfileVM"

        // 저장 형식(yyyy-MM-dd) -> 화면 표시 형식(yyyy. MM. dd)
        fun formatBirth(birthDate: String): String =
            birthDate.replace("-", ". ")
    }
}

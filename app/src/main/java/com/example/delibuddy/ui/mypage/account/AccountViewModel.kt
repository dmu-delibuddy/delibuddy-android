package com.example.delibuddy.ui.mypage.account

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.R
import com.example.delibuddy.data.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/*
    * 마이페이지 송금 계좌 도메인 (계좌 카드 + 계좌 변경 창이 함께 씀)
    * 저장 위치 : users/{uid} 의 bankName, account
*/
class AccountViewModel(
    private val userRepository: UserRepository = UserRepository(),
) : ViewModel() {

    // 화면에 보여줄 송금 계좌
    data class Account(
        val bankName: String = "",
        val number: String = "",
    ) {
        // 은행과 계좌번호가 모두 있어야 등록된 것
        val isRegistered: Boolean
            get() = bankName.isNotBlank() && number.isNotBlank()
    }

    // 로그인한 내 uid
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid

    // 화면에 띄울 안내 문구 (토스트로 보여준 뒤 null 로 비움)
    private val _message = MutableLiveData<Int?>()
    val message: LiveData<Int?> = _message

    // 저장 중인지 (중복 저장 방지)
    private val _saving = MutableLiveData(false)
    val saving: LiveData<Boolean> = _saving

    // 내 송금 계좌
    private val _account = MutableLiveData<Account>()
    val account: LiveData<Account> = _account

    init {
        loadAccount()
    }

    // users/{uid} 에서 은행, 계좌번호 한 번 불러오기
    private fun loadAccount() {
        val id = userId ?: return
        viewModelScope.launch {
            try {
                val user = userRepository.getById(id).await()
                _account.value = Account(
                    bankName = user?.bankName.orEmpty(),
                    number = user?.account.orEmpty(),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "계좌 불러오기 실패", e)
                _message.value = R.string.mypage_load_error
            }
        }
    }

    // 계좌번호 검사. 문제 있으면 안내 문구, 없으면 null
    @StringRes
    fun validate(number: String): Int? = when {
        number.isBlank() -> R.string.mypage_account_required
        number.any { it !in '0'..'9' } -> R.string.mypage_account_invalid
        else -> null
    }

    // 송금 계좌 저장 (users 문서의 은행, 계좌번호만 수정)
    fun save(bankName: String, number: String) {
        val id = userId ?: return
        if (_saving.value == true) return
        val updated = Account(bankName, number.trim())
        _saving.value = true
        viewModelScope.launch {
            try {
                userRepository.updateAccount(id, updated.bankName, updated.number).await()
                _account.value = updated
                _message.value = R.string.mypage_account_saved
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "계좌 저장 실패", e)
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
        private const val TAG = "AccountVM"
    }
}

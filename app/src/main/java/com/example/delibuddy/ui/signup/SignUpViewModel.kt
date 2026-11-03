package com.example.delibuddy.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.R
import com.example.delibuddy.data.auth.AuthRepository
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class SignUpViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val authRepository = AuthRepository()
    private val _isSendingVerification = MutableLiveData(false)
    val isSendingVerification: LiveData<Boolean> = _isSendingVerification
    private val _verificationMessage = MutableLiveData<Int?>()
    val verificationMessage: LiveData<Int?> = _verificationMessage

    val email: LiveData<String> = savedStateHandle.getLiveData("email", "")

    private val _password = MutableLiveData("")
    val password: LiveData<String> = _password
    private val _passwordConfirm = MutableLiveData("")
    val passwordConfirm: LiveData<String> = _passwordConfirm

    fun setPassword(value: String) {
        _password.value = value
    }

    fun setPasswordConfirm(value: String) {
        _passwordConfirm.value = value
    }

    fun setEmail(value: String) {
        savedStateHandle["email"] = value
    }

    fun sendVerificationEmail() {
        if (_isSendingVerification.value == true) return

        val emailId = email.value.orEmpty().trim()
        val passwordValue = password.value.orEmpty()
        val fullEmail = "$emailId@dongyang.ac.kr"
        val validationError = when {
            emailId.isBlank() || '@' in emailId ||
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(fullEmail).matches() ->
                R.string.signup_invalid_email

            passwordValue.length < 6 -> R.string.signup_password_too_short
            passwordValue != passwordConfirm.value -> R.string.signup_password_mismatch
            else -> null
        }
        if (validationError != null) {
            _verificationMessage.value = validationError
            return
        }

        _isSendingVerification.value = true
        _verificationMessage.value = null
        viewModelScope.launch {
            try {
                authRepository.sendSignUpVerification(fullEmail, passwordValue)
                _verificationMessage.value = R.string.signup_verification_sent
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _verificationMessage.value = when (e) {
                    is FirebaseAuthUserCollisionException -> R.string.signup_email_already_used
                    is FirebaseAuthWeakPasswordException -> R.string.signup_password_weak
                    else -> R.string.signup_verification_failed
                }
            } finally {
                _isSendingVerification.value = false
            }
        }
    }
}

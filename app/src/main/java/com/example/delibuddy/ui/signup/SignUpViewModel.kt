package com.example.delibuddy.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.R
import com.example.delibuddy.data.auth.AuthRepository
import com.example.delibuddy.data.auth.EmailNotVerifiedException
import com.example.delibuddy.data.auth.SignUpSessionMissingException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.example.delibuddy.data.user.UserModel
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignUpViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _isSendingVerification = MutableLiveData(false)
    val isSendingVerification: LiveData<Boolean> = _isSendingVerification

    private val _verificationMessage = MutableLiveData<Int?>()
    val verificationMessage: LiveData<Int?> = _verificationMessage

    private val _isVerificationEmailSent = MutableLiveData(false)
    val isVerificationEmailSent: LiveData<Boolean> = _isVerificationEmailSent

    private val _isCompletingSignUp = MutableLiveData(false)
    val isCompletingSignUp: LiveData<Boolean> = _isCompletingSignUp

    private val _isLoggedIn = MutableLiveData(false)
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _email = MutableLiveData("")
    val email: LiveData<String> = _email

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
        if (_email.value != value) {
            _isVerificationEmailSent.value = false
            _verificationMessage.value = null
        }
        _email.value = value
    }

    fun completeSignUp() {
        if (_isCompletingSignUp.value == true || _isSendingVerification.value == true) return

        _isCompletingSignUp.value = true
        _verificationMessage.value = null
        viewModelScope.launch {
            try {
                authRepository.completeSignUp(
                    UserModel(
                        univEmail = "${email.value.orEmpty().trim()}@dongyang.ac.kr",
                        birthDate = null,
                    )
                ).await()
                _isLoggedIn.value = authRepository.isLoggedIn()
                if (_isLoggedIn.value != true) {
                    _verificationMessage.value = R.string.signup_completion_failed
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _verificationMessage.value = when (e) {
                    is EmailNotVerifiedException -> R.string.signup_email_not_verified
                    is FirebaseNetworkException -> R.string.signup_verification_check_network_failed
                    is SignUpSessionMissingException,
                    is FirebaseAuthInvalidUserException -> R.string.signup_session_expired
                    else -> R.string.signup_completion_failed
                }
            } finally {
                _isCompletingSignUp.value = false
            }
        }
    }

    fun sendVerificationEmail() {
        if (_isSendingVerification.value == true || _isCompletingSignUp.value == true) return

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
                _isVerificationEmailSent.value = true
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

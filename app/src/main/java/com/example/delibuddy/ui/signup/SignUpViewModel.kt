package com.example.delibuddy.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.R
import com.example.delibuddy.data.auth.AuthRepository
import com.example.delibuddy.data.auth.EmailNotVerifiedException
import com.example.delibuddy.data.auth.SignUpSessionMissingException
import com.example.delibuddy.data.user.UserRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.example.delibuddy.data.user.UserModel
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _isSendingVerification = MutableLiveData(false)
    val isSendingVerification: LiveData<Boolean> = _isSendingVerification

    private val _verificationMessage = MutableLiveData<Int?>()
    val verificationMessage: LiveData<Int?> = _verificationMessage

    private val _isVerificationEmailSent = MutableLiveData(false)
    val isVerificationEmailSent: LiveData<Boolean> = _isVerificationEmailSent

    private val _isConfirmingVerification = MutableLiveData(false)
    val isConfirmingVerification: LiveData<Boolean> = _isConfirmingVerification

    private val _isReadyForBirthDate = MutableLiveData(false)
    val isReadyForBirthDate: LiveData<Boolean> = _isReadyForBirthDate

    fun onBirthDateNavigated() {
        _isReadyForBirthDate.value = false
    }

    private val _email = MutableLiveData("")
    val email: LiveData<String> = _email
    fun setEmail(value: String) {
        if (_email.value != value) {
            _isVerificationEmailSent.value = false
            _verificationMessage.value = null
        }
        _email.value = value
    }

    private val _password = MutableLiveData("")
    val password: LiveData<String> = _password
    fun setPassword(value: String) {
        _password.value = value
    }

    private val _passwordConfirm = MutableLiveData("")
    val passwordConfirm: LiveData<String> = _passwordConfirm
    fun setPasswordConfirm(value: String) {
        _passwordConfirm.value = value
    }

    private val _birthDate = MutableLiveData<String?>()
    val birthDate: LiveData<String?> = _birthDate
    fun setBirthDate(value: String?) {
        _birthDate.value = value
    }

    private val _bankName = MutableLiveData("")
    val bankName: LiveData<String> = _bankName
    fun setBankName(value: String) {
        _bankName.value = value
    }

    private val _account = MutableLiveData("")
    val account: LiveData<String> = _account
    fun setAccount(value: String) {
        _account.value = value
    }

    private val _isSavingProfile = MutableLiveData(false)
    val isSavingProfile: LiveData<Boolean> = _isSavingProfile

    private val _isRegistrationComplete = MutableLiveData(false)
    val isRegistrationComplete: LiveData<Boolean> = _isRegistrationComplete

    private val _profileError = MutableLiveData<Int?>()
    val profileError: LiveData<Int?> = _profileError

    fun onProfileErrorShown() {
        _profileError.value = null
    }

    fun onVerificationMessageShown() {
        _verificationMessage.value = null
    }

    fun finishRegistration(skipAccount: Boolean) {
        if (_isSavingProfile.value == true || _isRegistrationComplete.value == true) return

        val profile = createUserModel(skipAccount)
        val validationError = validateProfile(profile, skipAccount)
        _profileError.value = validationError
        if (validationError != null) return

        saveProfile(profile, skipAccount)
    }

    private fun saveProfile(profile: UserModel, skipAccount: Boolean) {
        _isSavingProfile.value = true
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: throw SignUpSessionMissingException()
                userRepository.update(
                    user.uid,
                    profile.copy(univEmail = user.email ?: profile.univEmail),
                )
                if (skipAccount) {
                    _bankName.value = ""
                    _account.value = ""
                }
                _isRegistrationComplete.value = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _profileError.value = e.toProfileError()
            } finally {
                _isSavingProfile.value = false
            }
        }
    }

    private fun createUserModel(skipAccount: Boolean): UserModel = UserModel(
        univEmail = "${email.value.orEmpty().trim()}@dongyang.ac.kr",
        birthDate = birthDate.value,
        bankName = bankName.value.orEmpty().trim().takeUnless { skipAccount },
        account = account.value.orEmpty().trim().takeUnless { skipAccount },
    )

    private fun validateProfile(profile: UserModel, skipAccount: Boolean): Int? = when {
        !skipAccount && (profile.bankName.isNullOrBlank() || profile.account.isNullOrBlank()) ->
            R.string.signup_account_required

        !skipAccount && profile.account.orEmpty().any { it !in '0'..'9' } ->
            R.string.signup_account_invalid

        else -> null
    }




    fun confirmEmailVerification() {
        if (_isConfirmingVerification.value == true || _isSendingVerification.value == true) return

        _isConfirmingVerification.value = true
        _verificationMessage.value = null
        viewModelScope.launch {
            try {
                authRepository.createUserDocument()
                _isReadyForBirthDate.value = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _verificationMessage.value = e.toConfirmationError()
            } finally {
                _isConfirmingVerification.value = false
            }
        }
    }

    fun isEmailValid(): Boolean {
        val emailId = email.value.orEmpty().trim()
        return emailId.isNotBlank() && '@' !in emailId &&
            android.util.Patterns.EMAIL_ADDRESS.matcher("$emailId@dongyang.ac.kr").matches()
    }

    fun sendVerificationEmail() {
        if (isVerificationBusy()) return

        val request = createSignUpRequest()
        val validationError = validateSignUp(request)
        if (validationError != null) {
            _verificationMessage.value = validationError
            return
        }

        requestVerificationEmail {
            val user = authRepository.signUp(request.email, request.password)
            authRepository.sendVerificationEmail(user)
        }
    }

    private fun requestVerificationEmail(request: suspend () -> Unit) {
        _isSendingVerification.value = true
        _verificationMessage.value = null
        viewModelScope.launch {
            try {
                request()
                _isVerificationEmailSent.value = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _verificationMessage.value = e.toVerificationError()
            } finally {
                _isSendingVerification.value = false
            }
        }
    }

    private fun isVerificationBusy(): Boolean =
        _isSendingVerification.value == true || _isConfirmingVerification.value == true

    private fun createSignUpRequest() = SignUpRequest(
        email = "${email.value.orEmpty().trim()}@dongyang.ac.kr",
        password = password.value.orEmpty(),
        passwordConfirm = passwordConfirm.value.orEmpty(),
    )

    private fun validateSignUp(request: SignUpRequest): Int? = when {
        !isEmailValid() -> R.string.signup_invalid_email
        request.password.length < 6 -> R.string.signup_password_too_short
        request.password != request.passwordConfirm -> R.string.signup_password_mismatch
        else -> null
    }

    private fun Exception.toVerificationError(): Int = when (this) {
        is FirebaseAuthUserCollisionException -> R.string.signup_email_already_used
        is FirebaseAuthWeakPasswordException -> R.string.signup_password_weak
        is SignUpSessionMissingException,
        is FirebaseAuthInvalidUserException -> R.string.signup_session_expired

        else -> R.string.signup_verification_failed
    }

    private fun Exception.toConfirmationError(): Int = when (this) {
        is EmailNotVerifiedException -> R.string.signup_email_not_verified
        is FirebaseNetworkException -> R.string.signup_verification_check_network_failed
        is SignUpSessionMissingException,
        is FirebaseAuthInvalidUserException -> R.string.signup_session_expired

        else -> R.string.signup_completion_failed
    }

    private fun Exception.toProfileError(): Int = when (this) {
        is FirebaseNetworkException -> R.string.signup_verification_check_network_failed
        is SignUpSessionMissingException,
        is FirebaseAuthInvalidUserException -> R.string.signup_session_expired

        else -> R.string.signup_completion_failed
    }

    private data class SignUpRequest(
        val email: String,
        val password: String,
        val passwordConfirm: String,
    )
}

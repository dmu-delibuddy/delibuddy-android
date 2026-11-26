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

    private data class SignUpRequest(
        val email: String,
        val password: String,
        val passwordConfirm: String,
    )

    private fun createSignUpRequest() = SignUpRequest(
        email = "${email.trim()}@dongyang.ac.kr",
        password = password,
        passwordConfirm = passwordConfirm,
    )

    private fun createUserModel(): UserModel = UserModel(
        univEmail = "${email.trim()}@dongyang.ac.kr",
        birthDate = birthDate,
        bankName = bankName?.trim(),
        account = account?.trim(),
    )

    private val _isEmailVerified = MutableLiveData(false)
    val isEmailVerified: LiveData<Boolean> = _isEmailVerified

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

    private val _isSavingProfile = MutableLiveData(false)
    val isSavingProfile: LiveData<Boolean> = _isSavingProfile

    private val _isRegistrationComplete = MutableLiveData(false)
    val isRegistrationComplete: LiveData<Boolean> = _isRegistrationComplete

    private val _profileError = MutableLiveData<Int?>()
    val profileError: LiveData<Int?> = _profileError

    fun onBirthDateNavigated() {
        _isReadyForBirthDate.value = false
    }

    fun onProfileErrorShown() {
        _profileError.value = null
    }

    fun onVerificationMessageShown() {
        _verificationMessage.value = null
    }

    private var _email = ""
    val email: String get() = _email

    private var _password = ""
    val password: String get() = _password

    private var _passwordConfirm = ""
    val passwordConfirm: String get() = _passwordConfirm

    private var _birthDate: String? = null
    val birthDate: String? get() = _birthDate

    private var _bankName: String? = null
    val bankName: String? get() = _bankName

    private var _account: String? = null
    val account: String? get() = _account

    fun setEmail(value: String) {
        if (_email != value) {
            _isVerificationEmailSent.value = false
            _verificationMessage.value = null
            _isEmailVerified.value = false
        }
        _email = value
    }

    fun setPassword(value: String) {
        _password = value
    }

    fun setPasswordConfirm(value: String) {
        _passwordConfirm = value
    }

    fun setBankName(value: String?) {
        _bankName = value
    }

    fun setBirthDate(value: String?) {
        _birthDate = value
    }

    fun setAccount(value: String?) {
        _account = value
    }

    fun finishRegistration() {
        if (_isSavingProfile.value == true || _isRegistrationComplete.value == true) return

        val profile = createUserModel()
        val validationError = validateProfile(profile)
        _profileError.value = validationError
        if (validationError != null) return

        saveProfile(profile)
    }

    private fun saveProfile(profile: UserModel) {
        _isSavingProfile.value = true
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: throw SignUpSessionMissingException()
                userRepository.update(
                    user.uid,
                    profile.copy(univEmail = user.email ?: profile.univEmail),
                )
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

    private fun validateProfile(profile: UserModel): Int? = when {
        profile.bankName == null && profile.account == null -> null

        profile.bankName.isNullOrBlank() || profile.account.isNullOrBlank() ->
            R.string.signup_account_required

        profile.account.orEmpty().any { it !in '0'..'9' } ->
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
                _isEmailVerified.value = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _verificationMessage.value = e.toConfirmationError()
            } finally {
                _isConfirmingVerification.value = false
            }
        }
    }

    fun moveToBirthDate() {
        if (_isEmailVerified.value != true) {
            _isEmailVerified.value = false
            _verificationMessage.value = R.string.signup_verification_required
            return
        }
        _isReadyForBirthDate.value = true
    }

    fun isEmailValid(): Boolean {
        val emailId = email.trim()
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
            val user = authRepository.createFirebaseUser(request.email, request.password)
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

    private fun validateSignUp(request: SignUpRequest): Int? = when {
        !isEmailValid() -> R.string.auth_email_invalid
        request.password.length < 6 -> R.string.signup_password_too_short
        request.password != request.passwordConfirm -> R.string.signup_password_mismatch
        else -> null
    }

    private fun Exception.toVerificationError(): Int = when (this) {
        is FirebaseAuthUserCollisionException -> R.string.signup_email_already_used
        is FirebaseAuthWeakPasswordException -> R.string.signup_password_weak
        is SignUpSessionMissingException,
        is FirebaseAuthInvalidUserException -> R.string.signup_session_expired

        else -> R.string.signup_verification_error
    }

    private fun Exception.toConfirmationError(): Int = when (this) {
        is EmailNotVerifiedException -> R.string.signup_email_not_verified
        is FirebaseNetworkException -> R.string.signup_verification_check_network_error
        is SignUpSessionMissingException,
        is FirebaseAuthInvalidUserException -> R.string.signup_session_expired

        else -> R.string.signup_completion_error
    }

    private fun Exception.toProfileError(): Int = when (this) {
        is FirebaseNetworkException -> R.string.signup_verification_check_network_error
        is SignUpSessionMissingException,
        is FirebaseAuthInvalidUserException -> R.string.signup_session_expired

        else -> R.string.signup_completion_error
    }
}

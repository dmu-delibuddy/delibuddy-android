package com.example.delibuddy.ui.signin

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.R
import com.example.delibuddy.data.auth.AuthRepository
import com.example.delibuddy.data.auth.AuthInvalidCredentialsException
import com.example.delibuddy.data.auth.AuthInvalidUserException
import com.example.delibuddy.data.auth.AuthNetworkException
import com.example.delibuddy.data.auth.EmailNotVerifiedException
import com.example.delibuddy.data.auth.SignUpIncompleteException
import com.example.delibuddy.data.auth.SignUpSessionMissingException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class SignInViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _isSigningIn = MutableLiveData(false)
    val isSigningIn: LiveData<Boolean> = _isSigningIn

    private val _isLoggedIn = MutableLiveData(false)
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _message = MutableLiveData<Int?>()
    val message: LiveData<Int?> = _message

    fun onMessageShown() {
        _message.value = null
    }

    fun signIn(emailId: String, password: String) {
        if (isSigningIn.value == true) return

        val id = emailId.trim()
        if (!isValidEmailId(id)) {
            _message.value = R.string.auth_email_invalid
            return
        }

        if (!isValidPassword(password)) {
            _message.value = R.string.signin_password_required
            return
        }

        _isSigningIn.value = true
        _message.value = null

        val email = "$id@dongyang.ac.kr"
        viewModelScope.launch {
            try {
                authRepository.signIn(email, password)
                _isLoggedIn.value = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _message.value = e.toSignInError()
            } finally {
                _isSigningIn.value = false
            }
        }
    }

    @StringRes
    private fun Exception.toSignInError(): Int = when (this) {
        is EmailNotVerifiedException,
        is SignUpIncompleteException -> R.string.signin_signup_incomplete
        is SignUpSessionMissingException -> R.string.common_error_not_logged_in
        is AuthInvalidCredentialsException -> R.string.signin_invalid_credentials
        is AuthInvalidUserException -> R.string.signin_invalid_user
        is AuthNetworkException -> R.string.signin_network_error
        else -> R.string.signin_error
    }

    private fun isValidEmailId(emailId: String): Boolean {
        val email = "$emailId@dongyang.ac.kr"
        return emailId.isNotBlank() &&
                '@' !in emailId &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.isNotEmpty()
    }
}

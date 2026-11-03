package com.example.delibuddy.ui.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.R
import com.example.delibuddy.data.auth.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignInViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val _isSigningIn = MutableLiveData(false)
    val isSigningIn: LiveData<Boolean> = _isSigningIn
    private val _isLoggedIn = MutableLiveData(false)
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn
    private val _message = MutableLiveData<Int?>()
    val message: LiveData<Int?> = _message

    fun signIn(emailId: String, password: String) {
        if (_isSigningIn.value == true) return
        val id = emailId.trim()
        val email = "$id@dongyang.ac.kr"
        if (id.isBlank() || '@' in id ||
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        ) {
            _message.value = R.string.signup_invalid_email
            return
        }
        if (password.isEmpty()) {
            _message.value = R.string.signin_password_required
            return
        }
        _isSigningIn.value = true
        _message.value = null
        viewModelScope.launch {
            try {
                authRepository.signIn(email, password).await()
                _isLoggedIn.value = authRepository.isLoggedIn()
                if (_isLoggedIn.value != true) {
                    _message.value = R.string.signin_signup_incomplete
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _message.value = R.string.signin_failed
            } finally {
                _isSigningIn.value = false
            }
        }
    }
}

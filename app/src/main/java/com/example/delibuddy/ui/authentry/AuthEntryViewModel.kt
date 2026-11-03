package com.example.delibuddy.ui.authentry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.data.auth.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AuthEntryViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _text = MutableLiveData("DELI-BUDDY")
    val text: LiveData<String> = _text

    private val _isLoggedIn = MutableLiveData(false)
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _isChecking = MutableLiveData(false)
    val isChecking: LiveData<Boolean> = _isChecking

    fun checkLoginStatus() {
        if (_isChecking.value == true) return
        _isChecking.value = true
        _isLoggedIn.value = false
        viewModelScope.launch {
            try {
                _isLoggedIn.value = authRepository.isLoggedIn()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _isLoggedIn.value = false
            } finally {
                _isChecking.value = false
            }
        }
    }
}

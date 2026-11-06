package com.example.delibuddy.ui.startup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.data.auth.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

enum class StartupState { CHECKING, HOME, AUTH_ENTRY, ERROR }

class StartupViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _state = MutableLiveData(StartupState.CHECKING)
    val state: LiveData<StartupState> = _state
    private var checkingJob: Job? = null

    init {
        checkLoginStatus()
    }

    fun checkLoginStatus() {
        if (checkingJob?.isActive == true) return
        _state.value = StartupState.CHECKING
        checkingJob = viewModelScope.launch {
            try {
                val loggedIn = withTimeout(15_000L) { authRepository.isLoggedIn() }
                _state.value = if (loggedIn) StartupState.HOME else StartupState.AUTH_ENTRY
            } catch (e: TimeoutCancellationException) {
                _state.value = StartupState.ERROR
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = StartupState.ERROR
            }
        }
    }
}

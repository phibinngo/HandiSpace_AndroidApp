package com.example.handispace.ui.auth.forgot_password

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.AuthRepository
import com.example.handispace.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var forgotPasswordState = mutableStateOf(ResultState<String>())
        private set

    fun resetPassword(email: String) {
        viewModelScope.launch {
            forgotPasswordState.value = ResultState(isLoading = true)
            forgotPasswordState.value = authRepository.resetPassword(email)
        }
    }

    fun resetState() {
        forgotPasswordState.value = ResultState()
    }
}
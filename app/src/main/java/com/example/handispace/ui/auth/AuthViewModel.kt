package com.example.handispace.ui.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.AuthRepository
import com.example.handispace.model.User
import com.example.handispace.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var loginState = mutableStateOf(ResultState<User>())
        private set

    var registerState = mutableStateOf(ResultState<User>())
        private set

    init {
        checkAutoLogin()
    }

    private fun checkAutoLogin() {
        viewModelScope.launch {
            val result = authRepository.checkAutoLogin()
            // 🔥 NẾU CÓ DATA HOẶC CÓ LỖI (BỊ KHÓA) THÌ ĐẨY RA UI ĐỂ BÁO
            if (result.data != null || result.errorMessage != null) {
                loginState.value = result
            }
        }
    }

    fun login(email: String, matKhau: String) {
        viewModelScope.launch {
            loginState.value = ResultState(isLoading = true)
            loginState.value = authRepository.login(email, matKhau)
        }
    }

    fun register(email: String, matKhau: String, hoTen: String, soDienThoai: String, username: String) {
        viewModelScope.launch {
            registerState.value = ResultState(isLoading = true)
            registerState.value = authRepository.register(email, matKhau, hoTen, soDienThoai, username)
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            loginState.value = ResultState(isLoading = true)
            loginState.value = authRepository.loginWithGoogle(idToken)
        }
    }

    fun clearLoginError() {
        loginState.value = ResultState()
    }

    fun resetRegisterState() {
        registerState.value = ResultState()
    }

    fun logout() {
        loginState.value = ResultState()
        registerState.value = ResultState()
    }
}
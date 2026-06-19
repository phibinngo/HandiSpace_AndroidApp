package com.example.handispace.ui.user.voucher

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.user.UserRepository
import com.example.handispace.data.repository.user.VoucherRepository
import com.example.handispace.model.Voucher
import com.example.handispace.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserVoucherViewModel @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var vouchersState = mutableStateOf(ResultState<List<Voucher>>())
        private set

    var savedVoucherIds = mutableStateOf<List<String>>(emptyList())
        private set

    // 🔥 BỔ SUNG: Chứa danh sách các mã voucher mà user đã từng dùng
    var usedVoucherIds = mutableStateOf<List<String>>(emptyList())
        private set

    init {
        loadVouchers()
    }

    fun loadVouchers() {
        viewModelScope.launch {
            vouchersState.value = ResultState(isLoading = true)
            vouchersState.value = voucherRepository.getActiveVouchers()

            val userResult = userRepository.getUserProfile()
            val userProfile = userResult.data // Tránh lỗi Smart Cast

            if (userProfile != null) {
                savedVoucherIds.value = userProfile.saved_vouchers
                // 🔥 Lấy list voucher_id đã dùng đổ vào UI
                usedVoucherIds.value = userProfile.used_vouchers.map { it.voucher_id }
            }
        }
    }

    fun saveVoucher(voucherId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = voucherRepository.saveVoucherToWallet(voucherId)
            if (result.data == true) {
                savedVoucherIds.value = savedVoucherIds.value + voucherId
                onResult(true, "Đã lưu Voucher vào kho!")
            } else {
                onResult(false, result.errorMessage ?: "Lưu thất bại")
            }
        }
    }

    fun applyCode(code: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = voucherRepository.saveVoucherByCode(code)
            val successMsg = result.data // Tránh lỗi Smart Cast

            if (successMsg != null) {
                loadVouchers()
                onResult(true, successMsg)
            } else {
                onResult(false, result.errorMessage ?: "Mã không hợp lệ")
            }
        }
    }
}
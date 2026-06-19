package com.example.handispace.ui.user.profile.myvoucher

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
class MyVoucherViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val voucherRepository: VoucherRepository
) : ViewModel() {

    var myVouchersState = mutableStateOf(ResultState<List<Voucher>>())
        private set

    var historyVouchersState = mutableStateOf(ResultState<List<Voucher>>())
        private set

    init {
        loadMyVouchers()
    }

    fun loadMyVouchers() {
        viewModelScope.launch {
            myVouchersState.value = ResultState(isLoading = true)
            historyVouchersState.value = ResultState(isLoading = true)

            val userResult = userRepository.getUserProfile()
            val userProfile = userResult.data

            if (userProfile != null) {
                val allVouchersResult = voucherRepository.getActiveVouchers()
                val allVouchers = allVouchersResult.data

                if (allVouchers != null) {
                    val myVouchers = allVouchers.filter { it.voucher_id in userProfile.saved_vouchers }
                    myVouchersState.value = ResultState(data = myVouchers)

                    // LỊCH SỬ ĐƯỢC SẮP XẾP MỚI NHẤT LÊN ĐẦU DỰA VÀO used_at
                    val sortedHistoryInfo = userProfile.used_vouchers.sortedByDescending { it.used_at?.seconds ?: 0 }

                    val historyList = sortedHistoryInfo.mapNotNull { info ->
                        allVouchers.find { it.voucher_id == info.voucher_id }
                    }

                    historyVouchersState.value = ResultState(data = historyList)
                } else {
                    myVouchersState.value = ResultState(errorMessage = allVouchersResult.errorMessage)
                    historyVouchersState.value = ResultState(errorMessage = allVouchersResult.errorMessage)
                }
            } else {
                myVouchersState.value = ResultState(errorMessage = userResult.errorMessage)
                historyVouchersState.value = ResultState(errorMessage = userResult.errorMessage)
            }
        }
    }
}
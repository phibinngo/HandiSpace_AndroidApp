package com.example.handispace.ui.user.profile.address

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.user.UserRepository
import com.example.handispace.model.Address
import com.example.handispace.model.User
import com.example.handispace.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var userState = mutableStateOf(ResultState<User>())
        private set

    init {
        loadUserAddresses()
    }

    private fun loadUserAddresses() {
        viewModelScope.launch {
            userState.value = ResultState(isLoading = true)
            userState.value = userRepository.getUserProfile()
        }
    }

    // LƯU ĐỊA CHỈ: Đã thêm onSuccess để chờ Firebase xử lý xong
    fun saveAddress(address: Address, onSuccess: () -> Unit) {
        val currentUser = userState.value.data ?: return
        val currentAddresses = currentUser.addresses.toMutableList()
        var newAddress = address

        if (currentAddresses.isEmpty()) {
            newAddress = newAddress.copy(is_default = true)
        }

        if (newAddress.is_default) {
            for (i in currentAddresses.indices) {
                currentAddresses[i] = currentAddresses[i].copy(is_default = false)
            }
        }

        val existingIndex = currentAddresses.indexOfFirst { it.address_id == newAddress.address_id }
        if (existingIndex != -1) {
            currentAddresses[existingIndex] = newAddress
        } else {
            val finalAddress = if (newAddress.address_id.isEmpty()) newAddress.copy(address_id = UUID.randomUUID().toString()) else newAddress
            currentAddresses.add(finalAddress)
        }

        updateFirebaseAddresses(currentAddresses, currentUser, onSuccess)
    }

    // XÓA ĐỊA CHỈ
    fun deleteAddress(addressId: String) {
        val currentUser = userState.value.data ?: return
        val currentAddresses = currentUser.addresses.toMutableList()
        val addressToDelete = currentAddresses.find { it.address_id == addressId }

        if (addressToDelete != null) {
            currentAddresses.remove(addressToDelete)

            if (addressToDelete.is_default && currentAddresses.isNotEmpty()) {
                currentAddresses[0] = currentAddresses[0].copy(is_default = true)
            }
            updateFirebaseAddresses(currentAddresses, currentUser)
        }
    }

    // HÀM CHUYÊN BIỆT: Update mảng Addresses và chạy onSuccess khi thành công
    private fun updateFirebaseAddresses(newAddresses: List<Address>, currentUser: User, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            userState.value = ResultState(isLoading = true)
            val result = userRepository.updateUserAddresses(newAddresses)

            if (result.data == true) {
                userState.value = ResultState(data = currentUser.copy(addresses = newAddresses))
                onSuccess?.invoke() // Gọi lệnh chuyển màn hình sau khi data đã lưu chắc chắn
            } else {
                userState.value = ResultState(errorMessage = result.errorMessage)
            }
        }
    }
}
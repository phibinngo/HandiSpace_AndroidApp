package com.example.handispace.ui.user.profile

import android.content.Context // 🔥 Nhớ import để xài Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.user.UserRepository
import com.example.handispace.model.User
import com.example.handispace.utils.ResultState
import com.example.handispace.utils.CloudinaryHelper // 🔥 IMPORT MÁY BƠM ẢNH
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var userState = mutableStateOf(ResultState<User>())
        private set

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            userState.value = ResultState(isLoading = true)
            userState.value = userRepository.getUserProfile()
        }
    }

    // 🔥 ĐÃ THÊM BIẾN context VÀO ĐỂ ĐỌC FILE ẢNH
    fun updateUserProfile(
        context: Context,
        newName: String,
        newPhone: String,
        newAvatarUrl: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val currentUser = userState.value.data ?: return

        viewModelScope.launch {
            userState.value = ResultState(isLoading = true) // Hiện trạng thái Đang Lưu

            // 🔥 BƯỚC 1: KIỂM TRA XEM CÓ PHẢI ẢNH MỚI CHỌN TỪ MÁY KHÔNG?
            var finalAvatarUrl = newAvatarUrl

            // Nếu link bắt đầu bằng "content://" hoặc "file://" có nghĩa là ảnh nằm trong máy (Uri nội bộ)
            if (newAvatarUrl.startsWith("content://") || newAvatarUrl.startsWith("file://")) {
                val cloudinaryHelper = CloudinaryHelper()

                // Gọi Cloudinary để nén và tải lên
                val uploadedUrl = cloudinaryHelper.uploadImage(context, Uri.parse(newAvatarUrl))

                if (uploadedUrl != null) {
                    finalAvatarUrl = uploadedUrl // Đổi link cục bộ thành link https xịn
                } else {
                    // Up ảnh thất bại thì dừng luôn
                    userState.value = ResultState(data = currentUser, errorMessage = "Tải ảnh lên thất bại!")
                    onResult(false, "Lỗi tải ảnh lên. Vui lòng thử lại!")
                    return@launch
                }
            }

            // 🔥 BƯỚC 2: TIẾN HÀNH LƯU DATABASE NHƯ BÌNH THƯỜNG
            val updatedUser = currentUser.copy(
                name = newName,
                phone = newPhone,
                avatar_url = finalAvatarUrl // Lưu link xịn vào db
            )

            val result = userRepository.updateUser(updatedUser)

            if (result.data == true) {
                userState.value = ResultState(data = updatedUser)
                onResult(true, "Cập nhật hồ sơ thành công!")
            } else {
                userState.value = ResultState(data = currentUser, errorMessage = result.errorMessage)
                onResult(false, result.errorMessage)
            }
        }
    }

    fun logout() {
        userRepository.logout()
    }
}
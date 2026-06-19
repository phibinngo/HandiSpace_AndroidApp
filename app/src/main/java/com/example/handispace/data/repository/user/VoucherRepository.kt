package com.example.handispace.data.repository.user

import com.example.handispace.model.User
import com.example.handispace.model.Voucher
import com.example.handispace.utils.ResultState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class VoucherRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val voucherCollection = firestore.collection("vouchers")
    private val userCollection = firestore.collection("users")

    suspend fun getActiveVouchers(): ResultState<List<Voucher>> {
        return try {
            val snapshot = voucherCollection
                .whereEqualTo("_active", true)
                .get()
                .await()
            val vouchers = snapshot.toObjects(Voucher::class.java)
            ResultState(data = vouchers)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi tải voucher")
        }
    }

    suspend fun saveVoucherToWallet(voucherId: String): ResultState<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: return ResultState(errorMessage = "Vui lòng đăng nhập!")

            // 🔥 BẢO VỆ BACKEND: Kiểm tra xem user đã dùng mã này chưa
            val user = userCollection.document(uid).get().await().toObject(User::class.java)
            val hasUsed = user?.used_vouchers?.any { it.voucher_id == voucherId } == true

            val voucherDoc = voucherCollection.document(voucherId).get().await()
            val voucherType = voucherDoc.getString("type") ?: ""

            if (hasUsed && voucherType != "shipping") {
                return ResultState(errorMessage = "Bạn đã sử dụng mã giảm giá này rồi!")
            }

            val dataToUpdate = mapOf("saved_vouchers" to FieldValue.arrayUnion(voucherId))
            userCollection.document(uid).set(dataToUpdate, SetOptions.merge()).await()

            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = "Lỗi lưu voucher: ${e.message}")
        }
    }

    suspend fun saveVoucherByCode(code: String): ResultState<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return ResultState(errorMessage = "Vui lòng đăng nhập!")

            val snapshot = voucherCollection
                .whereEqualTo("code", code.uppercase())
                .whereEqualTo("_active", true)
                .get()
                .await()

            if (snapshot.isEmpty) return ResultState(errorMessage = "Mã voucher không tồn tại hoặc đã hết hạn!")

            val voucher = snapshot.documents[0].toObject(Voucher::class.java)
                ?: return ResultState(errorMessage = "Lỗi đọc dữ liệu mã giảm giá!")

            val voucherId = voucher.voucher_id

            if (voucher.usage_limit > 0 && voucher.used_count >= voucher.usage_limit) {
                return ResultState(errorMessage = "Mã voucher này đã hết lượt sử dụng!")
            }

            // 🔥 BẢO VỆ BACKEND TỪ NHẬP CODE
            val user = userCollection.document(uid).get().await().toObject(User::class.java)
            val hasUsed = user?.used_vouchers?.any { it.voucher_id == voucherId } == true

            if (hasUsed && voucher.type != "shipping") {
                return ResultState(errorMessage = "Bạn đã sử dụng mã giảm giá này rồi!")
            }

            val dataToUpdate = mapOf("saved_vouchers" to FieldValue.arrayUnion(voucherId))
            userCollection.document(uid).set(dataToUpdate, SetOptions.merge()).await()

            ResultState(data = "Đã lưu mã ${code.uppercase()} thành công!")
        } catch (e: Exception) {
            ResultState(errorMessage = "Lỗi hệ thống: ${e.message}")
        }
    }
}
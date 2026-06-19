package com.example.handispace.data.repository.admin

import com.example.handispace.model.Voucher
import com.example.handispace.utils.ResultState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdminVoucherRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val voucherCollection = firestore.collection("vouchers")

    fun getAllVouchersAdminRealtime(): Flow<List<Voucher>> = callbackFlow {
        val listener = voucherCollection.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            trySend(snap?.documents?.mapNotNull { it.toObject(Voucher::class.java) } ?: emptyList())
        }
        awaitClose { listener.remove() }
    }

    suspend fun addVoucher(voucher: Voucher): ResultState<Boolean> {
        return try {
            val docRef = voucherCollection.document()
            val finalVoucher = voucher.copy(voucher_id = docRef.id, code = voucher.code.uppercase())
            docRef.set(finalVoucher).await()
            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi tạo voucher mới")
        }
    }

    suspend fun updateVoucher(voucher: Voucher): ResultState<Boolean> {
        return try {
            voucherCollection.document(voucher.voucher_id).set(voucher).await()
            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi cập nhật voucher")
        }
    }

    suspend fun deleteVoucher(voucherId: String): ResultState<Boolean> {
        return try {
            voucherCollection.document(voucherId).delete().await()
            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi xóa dữ liệu voucher")
        }
    }
}
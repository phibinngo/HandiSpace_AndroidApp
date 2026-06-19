package com.example.handispace.data.repository.user

import com.example.handispace.model.Notification
import com.example.handispace.utils.ResultState
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun getMyNotifications(): ResultState<List<Notification>> {
        return try {
            val userId = auth.currentUser?.uid ?: return ResultState(errorMessage = "Chưa đăng nhập")
            val snapshot = db.collection("notifications")
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            val list = snapshot.documents.map { doc ->
                Notification(
                    id = doc.id,
                    user_id = doc.getString("user_id") ?: "",
                    title = doc.getString("title") ?: "",
                    message = doc.getString("message") ?: "",
                    type = doc.getString("type") ?: "order",
                    is_read = doc.getBoolean("is_read") ?: doc.getBoolean("read") ?: false,
                    created_at = doc.getTimestamp("created_at")
                )
            }.sortedByDescending { it.created_at?.seconds ?: 0L }

            ResultState(data = list)
        } catch (e: Exception) {
            ResultState(errorMessage = "Lỗi tải thông báo: ${e.message}")
        }
    }

    fun listenUnreadCount(): Flow<Int> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(0)
            close()
            return@callbackFlow
        }

        val listener = db.collection("notifications")
            .whereEqualTo("user_id", userId)
            .whereEqualTo("is_read", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val count = snapshot?.size() ?: 0
                trySend(count)
            }

        awaitClose { listener.remove() }
    }

    suspend fun markAllAsRead(): ResultState<Boolean> {
        return try {
            val userId = auth.currentUser?.uid ?: return ResultState(errorMessage = "Chưa đăng nhập")

            val snapshot = db.collection("notifications")
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val batch = db.batch()
                for (doc in snapshot.documents) {
                    val isRead = doc.getBoolean("is_read") ?: doc.getBoolean("read") ?: false

                    if (!isRead) {
                        batch.update(doc.reference, "is_read", true)
                        batch.update(doc.reference, "read", true)
                    }
                }
                batch.commit().await()
            }
            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = "Lỗi cập nhật trạng thái đã đọc: ${e.message}")
        }
    }

    suspend fun pushNotification(userId: String, title: String, message: String, type: String = "order") {
        try {
            val ref = db.collection("notifications").document()
            val notiMap = hashMapOf(
                "id" to ref.id,
                "user_id" to userId,
                "title" to title,
                "message" to message,
                "type" to type,
                "is_read" to false,
                "created_at" to Timestamp.now()
            )
            ref.set(notiMap).await()
        } catch (_: Exception) {}
    }
}
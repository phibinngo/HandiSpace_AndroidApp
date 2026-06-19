package com.example.handispace.data.repository

import com.example.handispace.model.ChatRoom
import com.example.handispace.model.Message
import com.example.handispace.utils.ResultState
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    // 🔥 ID CỦA ADMIN (Đảm bảo ní đang xài đúng ID này bên Firestore nhé)
    private val ADMIN_UID = "axTeqn5PxRet3M5vYr1nvgbnp542"

    val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    // Hàm lấy danh sách phòng cho Admin
    fun getMyChatRoomsRealtime(): Flow<List<ChatRoom>> = callbackFlow {
        val myId = currentUserId
        if (myId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("chat_rooms")
            .whereArrayContains("participants", myId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                // Tự động kéo Tên và Avatar mới nhất từ bảng users
                CoroutineScope(Dispatchers.IO).launch {
                    val rooms = snapshot?.documents?.mapNotNull { doc ->
                        val room = doc.toObject(ChatRoom::class.java)

                        if (room != null) {
                            val otherId = room.participants.firstOrNull { it != myId } ?: ""

                            if (otherId == ADMIN_UID) {
                                room.copy(user_name = "CSKH HandiSpace", user_avatar = "")
                            } else if (otherId.isNotBlank()) {
                                try {
                                    val userDoc = db.collection("users").document(otherId).get().await()
                                    val realName = userDoc.getString("name") ?: "Khách hàng"
                                    val realAvatar = userDoc.getString("avatar_url") ?: ""

                                    room.copy(
                                        user_name = realName.ifBlank { "Khách hàng $otherId" },
                                        user_avatar = realAvatar
                                    )
                                } catch (e: Exception) {
                                    room
                                }
                            } else room
                        } else null
                    }?.sortedByDescending { it.last_timestamp } ?: emptyList()

                    trySend(rooms)
                }
            }
        awaitClose { listener.remove() }
    }

    // 🔥 HÀM LÕI: AI BẤM TRƯỚC CŨNG TẠO PHÒNG CHUẨN
    suspend fun createOrGetRoom(otherUserId: String): ResultState<String> {
        return try {
            val myId = currentUserId
            if (myId.isBlank()) return ResultState(errorMessage = "Chưa đăng nhập")

            // Tìm xem có phòng nào chứa myId không
            val snapshot = db.collection("chat_rooms")
                .whereArrayContains("participants", myId)
                .get().await()

            // Lọc tiếp xem phòng đó có chứa otherUserId không
            val existingRoom = snapshot.documents.find { doc ->
                val parts = doc.get("participants") as? List<String> ?: emptyList()
                parts.contains(otherUserId)
            }

            if (existingRoom != null) {
                // Đã chat với nhau rồi -> Trả về ID phòng cũ
                ResultState(data = existingRoom.id)
            } else {
                // Chưa từng chat -> Tạo phòng mới
                val newRoomRef = db.collection("chat_rooms").document()

                val newRoom = ChatRoom(
                    room_id = newRoomRef.id,
                    participants = listOf(myId, otherUserId), // [User, Admin] hoặc [Admin, User]
                    user_name = "", // Không lưu cứng tên ở đây nữa
                    user_avatar = "",
                    last_message = "Bắt đầu cuộc trò chuyện",
                    last_timestamp = Timestamp.now(),
                    unread_by_admin = 0,
                    unread_by_user = 0
                )
                newRoomRef.set(newRoom).await()
                ResultState(data = newRoomRef.id)
            }
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi tạo phòng chat")
        }
    }

    fun getMessagesRealtime(roomId: String): Flow<List<Message>> = callbackFlow {
        val listener = db.collection("chat_rooms").document(roomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { it.toObject(Message::class.java) } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun markRoomAsRead(roomId: String) {
        try {
            val myId = currentUserId
            if (myId.isBlank()) return

            val fieldToClear = if (myId == ADMIN_UID) "unread_by_admin" else "unread_by_user"
            db.collection("chat_rooms").document(roomId).update(fieldToClear, 0).await()
        } catch (e: Exception) { }
    }

    suspend fun sendMessage(roomId: String, text: String): ResultState<Boolean> {
        return try {
            val myId = currentUserId
            if (myId.isBlank()) return ResultState(errorMessage = "Lỗi chưa đăng nhập")
            val batch = db.batch()

            val roomRef = db.collection("chat_rooms").document(roomId)
            val msgRef = roomRef.collection("messages").document()

            val newMsg = Message(msgRef.id, myId, text, Timestamp.now())
            batch.set(msgRef, newMsg)

            val fieldToIncrement = if (myId == ADMIN_UID) "unread_by_user" else "unread_by_admin"

            batch.update(roomRef, mapOf(
                "last_message" to text,
                "last_timestamp" to Timestamp.now(),
                fieldToIncrement to FieldValue.increment(1)
            ))

            batch.commit().await()
            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi gửi tin nhắn")
        }
    }
}
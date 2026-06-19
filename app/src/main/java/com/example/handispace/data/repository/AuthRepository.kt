package com.example.handispace.data.repository

import com.example.handispace.model.User
import com.example.handispace.utils.Constants
import com.example.handispace.utils.ResultState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    suspend fun login(email: String, matKhau: String): ResultState<User> {
        return try {
            val ketQuaAuth = auth.signInWithEmailAndPassword(email, matKhau).await()
            val firebaseUser = ketQuaAuth.user

            if (firebaseUser != null) {
                firebaseUser.reload().await()

                if (firebaseUser.isEmailVerified) {
                    val document = db.collection(Constants.USERS).document(firebaseUser.uid).get().await()
                    val user = document.toObject(User::class.java)

                    if (user != null) {
                        if (user.is_disabled) {
                            auth.signOut()
                            ResultState(errorMessage = "Tài khoản của bạn đã bị khóa, vui lòng liên hệ admin email: phibinngo@gmail.com")
                        } else {
                            ResultState(data = user)
                        }
                    } else {
                        ResultState(errorMessage = "Không tìm thấy dữ liệu người dùng.")
                    }
                } else {
                    auth.signOut()
                    ResultState(errorMessage = "Vui lòng kiểm tra hộp thư để xác thực Email!")
                }
            } else {
                ResultState(errorMessage = "Đăng nhập thất bại.")
            }
        } catch (e: Exception) {
            ResultState(errorMessage = "Sai tài khoản hoặc mật khẩu!")
        }
    }

    suspend fun register(email: String, matKhau: String, hoTen: String, soDienThoai: String, username: String): ResultState<User> {
        return try {
            val usernameCheck = db.collection(Constants.USERS).whereEqualTo("username", username).get().await()
            if (!usernameCheck.isEmpty) {
                return ResultState(errorMessage = "Tên đăng nhập (Username) này đã tồn tại!")
            }

            val phoneCheck = db.collection(Constants.USERS).whereEqualTo("phone", soDienThoai).get().await()
            if (!phoneCheck.isEmpty) {
                return ResultState(errorMessage = "Số điện thoại này đã được đăng ký!")
            }

            val ketQuaAuth = auth.createUserWithEmailAndPassword(email, matKhau).await()
            val firebaseUser = ketQuaAuth.user

            if (firebaseUser != null) {
                val newUser = User(
                    uid = firebaseUser.uid,
                    name = hoTen,
                    username = username,
                    email = email,
                    phone = soDienThoai,
                    role = "customer"
                )
                db.collection(Constants.USERS).document(firebaseUser.uid).set(newUser).await()

                firebaseUser.sendEmailVerification().await()
                auth.signOut()

                ResultState(data = newUser)
            } else {
                ResultState(errorMessage = "Tạo tài khoản thất bại.")
            }
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Có lỗi xảy ra khi đăng ký.")
        }
    }


    suspend fun loginWithGoogle(idToken: String): ResultState<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val ketQuaAuth = auth.signInWithCredential(credential).await()
            val firebaseUser = ketQuaAuth.user

            if (firebaseUser != null) {
                val document = db.collection(Constants.USERS).document(firebaseUser.uid).get().await()

                if (document.exists()) {
                    val existingUser = document.toObject(User::class.java)
                    if (existingUser != null && existingUser.is_disabled) {
                        auth.signOut()
                        ResultState(errorMessage = "Tài khoản của bạn đã bị khóa, vui lòng liên hệ admin email: phibinngo@gmail.com")
                    } else {
                        ResultState(data = existingUser)
                    }
                } else {
                    val newUser = User(
                        uid = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "Khách hàng mới",
                        email = firebaseUser.email ?: "",
                        role = "customer",
                        avatar_url = firebaseUser.photoUrl?.toString() ?: ""
                    )
                    db.collection(Constants.USERS).document(firebaseUser.uid).set(newUser).await()
                    ResultState(data = newUser)
                }
            } else {
                ResultState(errorMessage = "Không thể xác thực bằng Google.")
            }
        } catch (e: Exception) {
            ResultState(errorMessage = "Lỗi đăng nhập Google: ${e.message}")
        }
    }

    suspend fun resetPassword(input: String): ResultState<String> {
        return try {
            var targetEmail = ""

            val emailQuery = db.collection(Constants.USERS).whereEqualTo("email", input).get().await()
            if (!emailQuery.isEmpty) {
                targetEmail = emailQuery.documents[0].getString("email") ?: ""
            } else {
                val phoneQuery = db.collection(Constants.USERS).whereEqualTo("phone", input).get().await()
                if (!phoneQuery.isEmpty) {
                    targetEmail = phoneQuery.documents[0].getString("email") ?: ""
                } else {
                    val usernameQuery = db.collection(Constants.USERS).whereEqualTo("username", input).get().await()
                    if (!usernameQuery.isEmpty) {
                        targetEmail = usernameQuery.documents[0].getString("email") ?: ""
                    }
                }
            }

            if (targetEmail.isNotEmpty()) {
                auth.sendPasswordResetEmail(targetEmail).await()
                ResultState(data = "Hệ thống đã gửi link đặt lại mật khẩu đến Email của tài khoản này!")
            } else {
                ResultState(errorMessage = "Không tìm thấy tài khoản nào khớp với thông tin!")
            }
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Có lỗi xảy ra.")
        }
    }

    suspend fun checkAutoLogin(): ResultState<User> {
        val firebaseUser = auth.currentUser ?: return ResultState()

        return try {
            val snapshot = db.collection("users").document(firebaseUser.uid).get().await()
            val user = snapshot.toObject(User::class.java)
            if (user != null) {
                if (user.is_disabled) {
                    auth.signOut()
                    // 🔥 TRẢ VỀ LỖI ĐỂ POPUP HIỆN LÊN THÔNG BÁO CHO USER LUÔN
                    ResultState(errorMessage = "Tài khoản của bạn đã bị khóa, vui lòng liên hệ admin email: phibinngo@gmail.com")
                } else {
                    ResultState(data = user)
                }
            } else {
                ResultState()
            }
        } catch (e: Exception) {
            ResultState()
        }
    }
}
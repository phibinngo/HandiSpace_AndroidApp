package com.example.handispace.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CloudinaryHelper {

    // 🔥 BẢO MẬT TỐI ĐA: Ní tự dán 3 cái mã của ní vào đây nha
    private val cloudinary = Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", "dhbpounmi",
            "api_key", "727612625894569",
            "api_secret", "FEyO9WNynQVR9a6Pc4a0ZxP3pNo"
        )
    )

    // Hàm nhận vào Uri (ảnh ní chọn từ đt), đẩy lên Cloudinary và trả về Link (String)
    suspend fun uploadImage(context: Context, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Chuyển Uri thành File tạm để Cloudinary nó đọc
                val file = getFileFromUri(context, imageUri)

                if (file != null) {
                    // 2. Phóng file lên server Cloudinary
                    val result = cloudinary.uploader().upload(file.absolutePath, ObjectUtils.emptyMap())

                    // 3. Up xong thì xóa cái file tạm trong đt đi cho đỡ rác máy
                    file.delete()

                    // 4. Lụm cái link HTTPs an toàn mang về
                    result["secure_url"] as? String
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // Hàm phụ trợ: Ép Uri thành File
    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)

            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
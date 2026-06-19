package com.example.handispace.data.repository.user

import com.example.handispace.model.Category
import com.example.handispace.model.Product
import com.example.handispace.utils.Constants
import com.example.handispace.utils.ResultState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun getCategories(): ResultState<List<Category>> {
        return try {
            val snapshot = db.collection(Constants.CATEGORIES).get().await()
            val categories = snapshot.documents.mapNotNull { it.toObject(Category::class.java) }

            ResultState(data = categories)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi khi tải danh mục sản phẩm.")
        }
    }

    suspend fun getProducts(categoryId: String? = null): ResultState<List<Product>> {
        return try {
            val query = if (categoryId != null && categoryId != "all") {
                db.collection(Constants.PRODUCTS).whereEqualTo("category_id", categoryId)
            } else {
                db.collection(Constants.PRODUCTS)
            }

            val snapshot = query.get().await()
            val products = snapshot.documents.mapNotNull { it.toObject(Product::class.java) }

            ResultState(data = products)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi khi tải danh sách sản phẩm.")
        }
    }

    suspend fun getProductById(productId: String): ResultState<Product> {
        return try {
            val snapshot = db.collection(Constants.PRODUCTS).document(productId).get().await()
            val product = snapshot.toObject(Product::class.java)

            if (product != null) {
                ResultState(data = product)
            } else {
                ResultState(errorMessage = "Không tìm thấy sản phẩm này.")
            }
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi khi tải chi tiết sản phẩm.")
        }
    }
}
package com.example.handispace.data.repository.admin

import com.example.handispace.model.Category
import com.example.handispace.model.Product
import com.example.handispace.utils.ResultState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdminProductRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    fun getCategoriesRealtime(): Flow<List<Category>> = callbackFlow {
        val listener = db.collection("categories")
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toObject(Category::class.java) } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    fun getProductsRealtime(): Flow<List<Product>> = callbackFlow {
        val listener = db.collection("products")
            .orderBy("created_at", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toObject(Product::class.java) } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun addProduct(product: Product): ResultState<Boolean> {
        return try {
            val docRef = db.collection("products").document()
            val newProduct = product.copy(product_id = docRef.id)
            docRef.set(newProduct).await()
            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi thêm sản phẩm")
        }
    }

    suspend fun updateProduct(product: Product): ResultState<Boolean> {
        return try {
            db.collection("products").document(product.product_id).set(product).await()
            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi cập nhật sản phẩm")
        }
    }

    suspend fun deleteProduct(productId: String): ResultState<Boolean> {
        return try {
            db.collection("products").document(productId).delete().await()
            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi xóa sản phẩm")
        }
    }
}
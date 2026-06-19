package com.example.handispace.data.repository.admin

import com.example.handispace.model.Category
import com.example.handispace.utils.ResultState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdminCategoryRepository @Inject constructor(
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

    suspend fun addCategory(category: Category): ResultState<Boolean> {
        return try {
            val docRef = db.collection("categories").document()
            val newCat = category.copy(category_id = docRef.id)
            docRef.set(newCat).await()
            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi thêm danh mục")
        }
    }

    suspend fun updateCategory(category: Category): ResultState<Boolean> {
        return try {
            db.collection("categories").document(category.category_id).set(category).await()
            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi cập nhật danh mục")
        }
    }

    suspend fun deleteCategory(categoryId: String): ResultState<Boolean> {
        return try {
            db.collection("categories").document(categoryId).delete().await()
            ResultState(data = true)
        } catch (e: Exception) {
            ResultState(errorMessage = e.message ?: "Lỗi xóa danh mục")
        }
    }
}
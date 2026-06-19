package com.example.handispace.ui.admin.categories

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.admin.AdminCategoryRepository
import com.example.handispace.data.repository.admin.AdminProductRepository
import com.example.handispace.model.Category
import com.example.handispace.model.Product
import com.example.handispace.utils.ResultState
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

// Lớp data ảo để gộp Category và số lượng Sản phẩm lại với nhau
data class CategoryWithCount(
    val category: Category,
    val productCount: Int
)

@HiltViewModel
class AdminCategoryViewModel @Inject constructor(
    private val categoryRepo: AdminCategoryRepository,
    private val productRepo: AdminProductRepository // Gọi Repo sản phẩm để đếm hàng
) : ViewModel() {

    val categories = mutableStateOf<List<Category>>(emptyList())
    val products = mutableStateOf<List<Product>>(emptyList()) // Chứa list sản phẩm để đếm
    val isLoading = mutableStateOf(true)

    // Các biến cho UI Lọc & Sắp xếp
    val searchQuery = mutableStateOf("")
    val sortOption = mutableStateOf("Tên A-Z")

    // State Thêm/Sửa/Xóa
    val actionState = mutableStateOf<ResultState<Boolean>?>(null)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            launch {
                categoryRepo.getCategoriesRealtime().collectLatest { cats ->
                    categories.value = cats
                    isLoading.value = false
                }
            }
            launch {
                // Kéo danh sách sản phẩm về để lấy count
                productRepo.getProductsRealtime().collectLatest { prods ->
                    products.value = prods
                }
            }
        }
    }

    // HÀM LÕI: TÌM KIẾM + ĐẾM SỐ LƯỢNG + SẮP XẾP
    fun getProcessedCategories(): List<CategoryWithCount> {
        // 1. Map thành object có Count
        var list = categories.value.map { cat ->
            val count = products.value.count { it.category_id == cat.category_id }
            CategoryWithCount(cat, count)
        }

        // 2. Tìm kiếm (Search)
        val query = searchQuery.value.trim().lowercase()
        if (query.isNotEmpty()) {
            list = list.filter { it.category.name.lowercase().contains(query) }
        }

        // 3. Sắp xếp
        list = when (sortOption.value) {
            "Tên A-Z" -> list.sortedBy { it.category.name }
            "Hàng hóa nhiều nhất" -> list.sortedByDescending { it.productCount }
            "Hàng hóa ít nhất" -> list.sortedBy { it.productCount }
            else -> list
        }

        return list
    }

    // --- CÁC HÀM XỬ LÝ DỮ LIỆU ---
    fun addCategory(name: String, type: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            actionState.value = ResultState(isLoading = true)
            val newCat = Category(name = name, type = type, created_at = Timestamp.now())
            actionState.value = categoryRepo.addCategory(newCat)
        }
    }

    fun updateCategory(categoryId: String, name: String, type: String, createdAt: Timestamp?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            actionState.value = ResultState(isLoading = true)
            val updatedCat = Category(category_id = categoryId, name = name, type = type, created_at = createdAt)
            actionState.value = categoryRepo.updateCategory(updatedCat)
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            actionState.value = ResultState(isLoading = true)
            actionState.value = categoryRepo.deleteCategory(categoryId)
        }
    }

    fun clearActionState() { actionState.value = null }
}
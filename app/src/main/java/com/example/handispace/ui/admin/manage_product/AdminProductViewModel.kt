package com.example.handispace.ui.admin.products

import android.content.Context // 🔥 Nhớ import để dùng Cloudinary
import android.net.Uri // 🔥 Xử lý list ảnh điện thoại
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.admin.AdminProductRepository
import com.example.handispace.model.Category
import com.example.handispace.model.Product
import com.example.handispace.utils.CloudinaryHelper // 🔥 Máy bơm ảnh
import com.example.handispace.utils.ResultState
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminProductViewModel @Inject constructor(
    private val repo: AdminProductRepository
) : ViewModel() {

    val allProducts = mutableStateOf<List<Product>>(emptyList())
    val categories = mutableStateOf<List<Category>>(emptyList())

    val selectedTab = mutableStateOf("Tất cả")
    val sortOption = mutableStateOf("Mới nhất")
    val isLoading = mutableStateOf(true)

    val addProductState = mutableStateOf<ResultState<Boolean>?>(null)
    val editProductState = mutableStateOf<ResultState<Boolean>?>(null)
    val deleteProductState = mutableStateOf<ResultState<Boolean>?>(null)

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                categories.value = repo.getCategoriesRealtime().first()
                allProducts.value = repo.getProductsRealtime().first()
            } catch (e: Exception) { } finally {
                isLoading.value = false
            }
        }
    }

    fun getFilteredAndSortedProducts(): List<Product> {
        var list = if (selectedTab.value == "Tất cả") {
            allProducts.value
        } else {
            allProducts.value.filter { it.category_name == selectedTab.value }
        }

        list = when (sortOption.value) {
            "Giá tăng dần" -> list.sortedBy { it.price }
            "Giá giảm dần" -> list.sortedByDescending { it.price }
            "Tên A-Z" -> list.sortedBy { it.name }
            else -> list
        }
        return list
    }

    // 🔥 HÀM ADD ĐÃ NÂNG CẤP XỬ LÝ ẢNH
    fun addProduct(
        context: Context,
        name: String,
        priceStr: String,
        quantityStr: String,
        description: String,
        category: Category?,
        imageUris: List<Uri> // Nhận mảng hình từ đt
    ) {
        viewModelScope.launch {
            if (name.isBlank() || priceStr.isBlank() || quantityStr.isBlank() || category == null) {
                addProductState.value = ResultState(errorMessage = "Vui lòng nhập đủ thông tin và chọn danh mục!")
                return@launch
            }
            val price = priceStr.toDoubleOrNull() ?: 0.0
            val quantity = quantityStr.toIntOrNull() ?: 0

            if (price <= 0 || quantity < 0) {
                addProductState.value = ResultState(errorMessage = "Giá và số lượng phải hợp lệ!")
                return@launch
            }

            addProductState.value = ResultState(isLoading = true) // HIỆN LOADING

            // 🔥 BẮT ĐẦU VÒNG LẶP UP ẢNH
            val cloudinaryHelper = CloudinaryHelper()
            val uploadedImageUrls = mutableListOf<String>()

            for (uri in imageUris) {
                val url = cloudinaryHelper.uploadImage(context, uri)
                if (url != null) {
                    uploadedImageUrls.add(url)
                } else {
                    addProductState.value = ResultState(errorMessage = "Lỗi khi up 1 số ảnh, vui lòng thử lại!")
                    return@launch
                }
            }

            val catId = getCategoryId(category)

            val newProduct = Product(
                category_id = catId,
                category_name = category.name,
                name = name,
                description = description,
                price = price,
                quantity = quantity,
                images = uploadedImageUrls, // 🔥 Nhét list ảnh thật vào đây
                created_at = Timestamp.now()
            )
            addProductState.value = repo.addProduct(newProduct)
        }
    }

    // 🔥 HÀM EDIT CŨNG ĐƯỢC NÂNG CẤP LƯU ẢNH
    fun updateProduct(
        context: Context,
        productId: String,
        name: String,
        priceStr: String,
        quantityStr: String,
        description: String,
        category: Category?,
        existingImagesOrUris: List<String> // Có thể là link HTTP cũ, có thể là URI cục bộ mới
    ) {
        viewModelScope.launch {
            if (name.isBlank() || priceStr.isBlank() || quantityStr.isBlank() || category == null) {
                editProductState.value = ResultState(errorMessage = "Vui lòng nhập đủ thông tin!")
                return@launch
            }
            val price = priceStr.toDoubleOrNull() ?: 0.0
            val quantity = quantityStr.toIntOrNull() ?: 0

            if (price <= 0 || quantity < 0) {
                editProductState.value = ResultState(errorMessage = "Giá và số lượng phải hợp lệ!")
                return@launch
            }

            editProductState.value = ResultState(isLoading = true)

            // 🔥 PHÂN LOẠI ẢNH (Ảnh mạng cũ giữ nguyên, ảnh Uri mới thì đem đi up)
            val cloudinaryHelper = CloudinaryHelper()
            val finalImageUrls = mutableListOf<String>()

            for (item in existingImagesOrUris) {
                if (item.startsWith("http")) {
                    finalImageUrls.add(item) // Ảnh cũ của Firebase, lấy xài tiếp
                } else {
                    // Ảnh mới chọn, bắt đầu đem đi nén & úp
                    val url = cloudinaryHelper.uploadImage(context, Uri.parse(item))
                    if (url != null) finalImageUrls.add(url)
                }
            }

            val catId = getCategoryId(category)

            val updatedProduct = Product(
                product_id = productId,
                category_id = catId,
                category_name = category.name,
                name = name,
                description = description,
                price = price,
                quantity = quantity,
                images = finalImageUrls, // 🔥 Cập nhật mảng ảnh đã trộn
                created_at = Timestamp.now()
            )
            editProductState.value = repo.updateProduct(updatedProduct)
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            deleteProductState.value = ResultState(isLoading = true)
            deleteProductState.value = repo.deleteProduct(productId)
        }
    }

    private fun getCategoryId(category: Category): String {
        return try {
            category.javaClass.getMethod("getCategory_id").invoke(category) as? String ?: ""
        } catch (e: Exception) {
            try { category.javaClass.getMethod("getId").invoke(category) as? String ?: "" } catch (_: Exception) { "" }
        }
    }
}
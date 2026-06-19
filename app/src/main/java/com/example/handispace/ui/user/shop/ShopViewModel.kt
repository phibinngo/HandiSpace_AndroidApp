package com.example.handispace.ui.user.shop

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.user.ProductRepository
import com.example.handispace.model.Category
import com.example.handispace.model.Product
import com.example.handispace.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ShopSortOption(val label: String) {
    DEFAULT("Mặc định"),
    NAME_ASC("Tên (A-Z)"),
    PRICE_ASC("Giá tăng dần"),
    PRICE_DESC("Giá giảm dần")
}

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    var categoryState = mutableStateOf(ResultState<List<Category>>())
    var productState = mutableStateOf(ResultState<List<Product>>())

    private var allProductsRaw: List<Product> = emptyList()

    var selectedCategoryId = mutableStateOf("all")
    var currentSortOption = mutableStateOf(ShopSortOption.DEFAULT)

    var isRefreshing = mutableStateOf(false)

    init {
        getCategories()
        fetchInitialProducts()
    }

    private fun getCategories() {
        viewModelScope.launch {
            categoryState.value = productRepository.getCategories()
        }
    }

    private fun fetchInitialProducts() {
        viewModelScope.launch {
            productState.value = ResultState(isLoading = true)
            val result = productRepository.getProducts("all")

            val fetchedData = result.data
            if (fetchedData != null) {
                allProductsRaw = fetchedData
                applyFiltersAndSort()
            } else {
                productState.value = result
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            isRefreshing.value = true
            val result = productRepository.getProducts("all")

            val fetchedData = result.data
            if (fetchedData != null) {
                allProductsRaw = fetchedData
                currentSortOption.value = ShopSortOption.DEFAULT
                applyFiltersAndSort()
            }
            isRefreshing.value = false
        }
    }

    fun selectCategory(categoryId: String) {
        selectedCategoryId.value = categoryId
        currentSortOption.value = ShopSortOption.DEFAULT
        applyFiltersAndSort()
    }

    fun selectSortOption(option: ShopSortOption) {
        currentSortOption.value = option
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        // 1. Lọc theo danh mục trước
        val filteredList = if (selectedCategoryId.value == "all") {
            allProductsRaw
        } else {
            allProductsRaw.filter { it.category_id == selectedCategoryId.value }
        }

        // 🔥 2. TÁCH LÀM 2 NHÓM: CÒN HÀNG VÀ HẾT HÀNG
        val inStock = filteredList.filter { it.quantity > 0 }
        val outOfStock = filteredList.filter { it.quantity <= 0 }

        // 3. Xử lý sắp xếp / Random cho nhóm CÒN HÀNG
        val sortedInStock = when (currentSortOption.value) {
            ShopSortOption.DEFAULT -> inStock.shuffled() // Random
            ShopSortOption.NAME_ASC -> inStock.sortedBy { it.name }
            ShopSortOption.PRICE_ASC -> inStock.sortedBy { it.price }
            ShopSortOption.PRICE_DESC -> inStock.sortedByDescending { it.price }
        }

        // 4. Xử lý sắp xếp / Random cho nhóm HẾT HÀNG
        val sortedOutOfStock = when (currentSortOption.value) {
            ShopSortOption.DEFAULT -> outOfStock.shuffled() // Random
            ShopSortOption.NAME_ASC -> outOfStock.sortedBy { it.name }
            ShopSortOption.PRICE_ASC -> outOfStock.sortedBy { it.price }
            ShopSortOption.PRICE_DESC -> outOfStock.sortedByDescending { it.price }
        }

        // 🔥 5. GỘP LẠI: Hàng còn nằm chễm chệ ở trên, hàng hết bị tống xuống đít danh sách!
        productState.value = ResultState(data = sortedInStock + sortedOutOfStock)
    }
}
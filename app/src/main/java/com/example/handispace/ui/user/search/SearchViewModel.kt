package com.example.handispace.ui.user.search

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

enum class SortOption(val label: String) {
    DEFAULT("Mặc định"),
    PRICE_ASC("Giá: Thấp đến Cao"),
    PRICE_DESC("Giá: Cao đến Thấp"),
    NAME_ASC("Tên: A - Z"),
    NAME_DESC("Tên: Z - A")
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    var searchState = mutableStateOf(ResultState<List<Product>>())
        private set

    var categoryState = mutableStateOf(ResultState<List<Category>>())
        private set

    var currentKeyword = mutableStateOf("")
    var selectedCategories = mutableStateOf<Set<String>>(emptySet())
    var currentSortOption = mutableStateOf(SortOption.DEFAULT)

    private var allProducts = listOf<Product>()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            categoryState.value = ResultState(isLoading = true)
            searchState.value = ResultState(isLoading = true)

            categoryState.value = productRepository.getCategories()

            val prodResult = productRepository.getProducts()
            if (prodResult.data != null) {
                allProducts = prodResult.data!!
                // FIX YÊU CẦU 1: Gán thành list rỗng để ban đầu không hiện gì cả
                searchState.value = ResultState(data = emptyList())
            } else {
                searchState.value = ResultState(errorMessage = prodResult.errorMessage)
            }
        }
    }

    fun filterAndSortProducts(
        keyword: String,
        categories: Set<String>,
        sortOption: SortOption
    ) {
        currentKeyword.value = keyword
        selectedCategories.value = categories
        currentSortOption.value = sortOption

        // Nếu không gõ gì và không lọc gì -> Trả về rỗng (Để trống màn hình)
        if (keyword.isBlank() && categories.isEmpty() && sortOption == SortOption.DEFAULT) {
            searchState.value = ResultState(data = emptyList())
            return
        }

        var filteredList = allProducts

        // 1. Lọc theo từ khóa (BÂY GIỜ CHỈ TÌM TRONG TÊN SẢN PHẨM)
        if (keyword.isNotBlank()) {
            filteredList = filteredList.filter {
                it.name.contains(keyword, ignoreCase = true)
            }
        }

        if (categories.isNotEmpty()) {
            filteredList = filteredList.filter { it.category_id in categories }
        }

        filteredList = when (sortOption) {
            SortOption.DEFAULT -> filteredList
            SortOption.PRICE_ASC -> filteredList.sortedBy { it.price }
            SortOption.PRICE_DESC -> filteredList.sortedByDescending { it.price }
            SortOption.NAME_ASC -> filteredList.sortedBy { it.name }
            SortOption.NAME_DESC -> filteredList.sortedByDescending { it.name }
        }

        searchState.value = ResultState(data = filteredList)
    }
}
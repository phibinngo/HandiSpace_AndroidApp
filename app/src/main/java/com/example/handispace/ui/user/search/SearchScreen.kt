package com.example.handispace.ui.user.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.handispace.navigation.Routes
import com.example.handispace.ui.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, viewModel: SearchViewModel) {
    val state = viewModel.searchState.value
    val catState = viewModel.categoryState.value

    var keyword by remember { mutableStateOf(viewModel.currentKeyword.value) }
    var showFilterDialog by remember { mutableStateOf(false) }

    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)
    val gray200 = Color(0xFFE5E7EB)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = keyword,
                        onValueChange = { keyword = it },
                        modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                        textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                        placeholder = { Text("Tìm kiếm sản phẩm...", fontSize = 14.sp, color = Color.Gray) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = gray50,
                            focusedContainerColor = gray50,
                            unfocusedBorderColor = gray200,
                            focusedBorderColor = orange600
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            viewModel.filterAndSortProducts(keyword, viewModel.selectedCategories.value, viewModel.currentSortOption.value)
                        })
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Trở lại", tint = orange600)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.filterAndSortProducts(keyword, viewModel.selectedCategories.value, viewModel.currentSortOption.value)
                    }) {
                        Icon(Icons.Filled.Search, contentDescription = "Tìm kiếm", tint = Color.Gray)
                    }
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Bộ lọc", tint = orange600)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = gray50
    ) { padding ->

        if (showFilterDialog) {
            FilterSortDialog(
                categories = catState.data ?: emptyList(),
                initialSelectedCats = viewModel.selectedCategories.value,
                initialSort = viewModel.currentSortOption.value,
                onDismiss = { showFilterDialog = false },
                onApply = { selectedCats, sortOpt ->
                    viewModel.filterAndSortProducts(keyword, selectedCats, sortOpt)
                    showFilterDialog = false
                }
            )
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = orange600) }
            } else if (state.data != null) {
                if (state.data!!.isEmpty()) {
                    if (keyword.isBlank() && viewModel.selectedCategories.value.isEmpty()) {
                        // ĐỂ TRỐNG HOÀN TOÀN NHƯ YÊU CẦU, KHÔNG GHI GÌ HẾT
                    } else {
                        // Khi có tìm kiếm nhưng không ra kết quả
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Không tìm thấy sản phẩm nào phù hợp", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.data!!) { prod ->
                            ProductCard(product = prod, onClick = {
                                navController.navigate(Routes.PRODUCT_DETAIL.replace("{productId}", prod.product_id))
                            })
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENT POPUP CHUẨN XÁC: RÚT GỌN/XEM THÊM + DROPDOWN
// ==========================================
@Composable
fun FilterSortDialog(
    categories: List<com.example.handispace.model.Category>,
    initialSelectedCats: Set<String>,
    initialSort: SortOption,
    onDismiss: () -> Unit,
    onApply: (Set<String>, SortOption) -> Unit
) {
    var tempSelectedCats by remember { mutableStateOf(initialSelectedCats) }
    var tempSort by remember { mutableStateOf(initialSort) }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    var showDropdown by remember { mutableStateOf(false) }

    val orange600 = Color(0xFFEA580C)
    val orange50 = Color(0xFFFFF7ED)
    val gray100 = Color(0xFFF3F4F6)
    val gray300 = Color(0xFFD1D5DB)
    val gray800 = Color(0xFF1F2937)

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().heightIn(max = 650.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Tiêu đề
                Box(modifier = Modifier.fillMaxWidth().background(gray100).padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("PHÂN LOẠI & SẮP XẾP", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = gray800)
                }

                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false).padding(16.dp).verticalScroll(rememberScrollState())
                ) {
                    // ==============================
                    // 1. CHỌN DANH MỤC (Logic Rút gọn / Xem thêm)
                    // ==============================
                    Text("Danh mục sản phẩm", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = gray800)
                    Spacer(modifier = Modifier.height(12.dp))

                    val itemsToDisplay = mutableListOf<Pair<String, String>>()
                    if (categories.size <= 6) {
                        categories.forEach { itemsToDisplay.add(it.name to it.category_id) }
                    } else {
                        if (isCategoryExpanded) {
                            categories.forEach { itemsToDisplay.add(it.name to it.category_id) }
                            itemsToDisplay.add("Rút gọn ▲" to "LESS_BTN") // Thêm nút Rút gọn
                        } else {
                            categories.take(5).forEach { itemsToDisplay.add(it.name to it.category_id) }
                            itemsToDisplay.add("Xem thêm ▼" to "MORE_BTN") // Thêm nút Xem thêm
                        }
                    }

                    val chunkedCategories = itemsToDisplay.chunked(2)
                    chunkedCategories.forEach { rowCats ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowCats.forEach { (catName, catId) ->
                                if (catId == "MORE_BTN" || catId == "LESS_BTN") {
                                    // NÚT XEM THÊM HOẶC RÚT GỌN
                                    Box(
                                        modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(6.dp))
                                            .background(gray100).clickable { isCategoryExpanded = !isCategoryExpanded },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(catName, fontSize = 13.sp, color = gray800, fontWeight = FontWeight.Medium)
                                    }
                                } else {
                                    // NÚT DANH MỤC BÌNH THƯỜNG
                                    val isSelected = tempSelectedCats.contains(catId)
                                    Box(
                                        modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) orange50 else gray100)
                                            .border(1.dp, if (isSelected) orange600 else Color.Transparent, RoundedCornerShape(6.dp))
                                            .clickable {
                                                tempSelectedCats = if (isSelected) tempSelectedCats - catId else tempSelectedCats + catId
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(catName, fontSize = 13.sp, color = if (isSelected) orange600 else gray800, textAlign = TextAlign.Center, maxLines = 1)
                                    }
                                }
                            }
                            // TẠO Ô TRỐNG TÀNG HÌNH (Xóa nền xám)
                            if (rowCats.size == 1) {
                                Spacer(modifier = Modifier.weight(1f).height(44.dp).background(Color.Transparent))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = gray300, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // ==============================
                    // 2. CHỌN SẮP XẾP (Dropdown đổ xuống)
                    // ==============================
                    Text("Sắp xếp theo", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = gray800)
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(6.dp))
                                .border(1.dp, gray300, RoundedCornerShape(6.dp)).clickable { showDropdown = true }.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(tempSort.label, fontSize = 14.sp, color = gray800)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = gray800)
                        }

                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.8f).background(Color.White)
                        ) {
                            SortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label, fontSize = 14.sp, color = if (tempSort == option) orange600 else gray800) },
                                    onClick = {
                                        tempSort = option
                                        showDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // ==============================
                // 3. HAI NÚT BẤM DƯỚI CÙNG
                // ==============================
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            tempSelectedCats = emptySet()
                            tempSort = SortOption.DEFAULT
                            isCategoryExpanded = false
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, gray300)
                    ) { Text("Thiết lập lại", color = gray800, fontWeight = FontWeight.Medium) }

                    Button(
                        onClick = { onApply(tempSelectedCats, tempSort) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = orange600)
                    ) { Text("Áp dụng", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
package com.example.handispace.ui.user.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handispace.navigation.Routes
import com.example.handispace.ui.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(navController: NavController, viewModel: ShopViewModel) {
    val productState = viewModel.productState.value
    val categoryState = viewModel.categoryState.value

    val selectedCatId = viewModel.selectedCategoryId.value
    var showSortMenu by remember { mutableStateOf(false) }

    val orange600 = Color(0xFFEA580C)
    val gray100 = Color(0xFFF5F5F5)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(gray100)
                            .clickable { navController.navigate(Routes.SEARCH) }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = "Tìm", tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tìm kiếm sản phẩm...", color = Color.Gray, fontSize = 14.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.CART) }) {
                        Icon(Icons.Outlined.ShoppingCart, contentDescription = "Giỏ", tint = orange600)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Routes.AI_CHAT) },
                containerColor = Color(0xFFEA580C),
                contentColor = Color.White,
                shape = RoundedCornerShape(50)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = "AI", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hỏi AI", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        containerColor = gray100
    ) { padding ->

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // =====================================
            // THANH DANH MỤC TRƯỢT NGANG & NÚT SORT
            // =====================================
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Row(modifier = Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {

                    LazyRow(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            CategoryChip(text = "Tất cả", isSelected = selectedCatId == "all") {
                                viewModel.selectCategory("all")
                            }
                        }

                        if (categoryState.data != null) {
                            items(categoryState.data!!) { cat ->
                                CategoryChip(text = cat.name, isSelected = selectedCatId == cat.category_id) {
                                    viewModel.selectCategory(cat.category_id)
                                }
                            }
                        }
                    }

                    // ĐÃ XÓA NÚT REFRESH Ở ĐÂY RỒI NHA NÍ

                    // NÚT SẮP XẾP ĐÃ RÚT GỌN
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Filled.Sort, contentDescription = "Sắp xếp", tint = if (viewModel.currentSortOption.value != ShopSortOption.DEFAULT) orange600 else Color.Gray)
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            ShopSortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label, color = if (viewModel.currentSortOption.value == option) orange600 else Color.Black) },
                                    onClick = {
                                        viewModel.selectSortOption(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // =====================================
            // LƯỚI SẢN PHẨM KHÔNG BỊ CHẶN UI
            // =====================================
            if (productState.data != null) {
                // Đã có dữ liệu thì cứ hiển thị mượt mà
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(productState.data!!) { prod ->
                        ProductCard(product = prod, onClick = {
                            navController.navigate(Routes.PRODUCT_DETAIL.replace("{productId}", prod.product_id))
                        })
                    }
                }
            } else {
                // Chỉ quay vòng tròn khi app vừa cài đặt, mở lên chưa kịp gọi Firebase
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = orange600)
                }
            }
        }
    }
}

@Composable
fun CategoryChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFFFFF7ED) else Color(0xFFF3F4F6))
            .border(1.dp, if (isSelected) Color(0xFFEA580C) else Color.Transparent, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) Color(0xFFEA580C) else Color(0xFF4B5563), fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
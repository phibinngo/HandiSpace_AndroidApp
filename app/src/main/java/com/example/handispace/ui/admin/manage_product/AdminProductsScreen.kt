package com.example.handispace.ui.admin.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.handispace.model.Product
import com.example.handispace.navigation.Routes
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductsScreen(
    navController: NavController,
    viewModel: AdminProductViewModel = hiltViewModel()
) {
    val selectedTab = viewModel.selectedTab.value
    val sortOption = viewModel.sortOption.value
    val filteredProducts = viewModel.getFilteredAndSortedProducts()
    val categories = viewModel.categories.value

    // 🔥 FIX LỖI HIỂN THỊ LỬNG LƠ: Ép cuộn về vị trí đầu tiên khi đổi bộ lọc
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 🔥 TỰ ĐỘNG LÀM TƯƠI DATA KHI QUAY LẠI MÀN HÌNH DANH SÁCH
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LaunchedEffect(selectedTab, sortOption) {
        if (filteredProducts.isNotEmpty()) {
            coroutineScope.launch { listState.scrollToItem(0) }
        }
    }

    val tabNames = listOf("Tất cả") + categories.map { it.name }
    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)

    var showSortMenu by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    // Hiển thị popup xác nhận xóa nếu có sản phẩm được chọn để xóa
    productToDelete?.let { prod ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = orange600),
                    onClick = {
                        viewModel.deleteProduct(prod.product_id)
                        productToDelete = null
                    }
                ) { Text("Đồng ý") }
            },
            dismissButton = {
                OutlinedButton(onClick = { productToDelete = null }) { Text("Hủy bỏ", color = Color.Gray) }
            },
            title = { Text("Xác nhận xóa", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("Bạn có chắc chắn muốn xóa sản phẩm \"${prod.name}\" khỏi hệ thống không?") },
            shape = RoundedCornerShape(12.dp),
            containerColor = Color.White
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.ADMIN_ADD_PRODUCT) },
                containerColor = orange600,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) { Icon(Icons.Filled.Add, contentDescription = "Thêm sản phẩm") }
        },
        containerColor = gray50
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // Sửa chữ thành Tổng sản phẩm
            Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Tổng: ${filteredProducts.size} sản phẩm", fontWeight = FontWeight.Bold, color = Color.Gray)
                Box {
                    TextButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Filled.Sort, contentDescription = null, tint = orange600, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(sortOption, color = orange600, fontSize = 13.sp)
                    }
                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        listOf("Mới nhất", "Giá tăng dần", "Giá giảm dần", "Tên A-Z").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = if (sortOption == option) orange600 else Color.Black) },
                                onClick = { viewModel.sortOption.value = option; showSortMenu = false }
                            )
                        }
                    }
                }
            }

            ScrollableTabRow(
                selectedTabIndex = tabNames.indexOf(selectedTab).coerceAtLeast(0),
                containerColor = Color.White,
                contentColor = orange600,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    if (tabPositions.isNotEmpty()) {
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[tabNames.indexOf(selectedTab).coerceAtLeast(0)]),
                            color = orange600, height = 3.dp
                        )
                    }
                }
            ) {
                tabNames.forEach { tabName ->
                    Tab(
                        selected = selectedTab == tabName,
                        onClick = { viewModel.selectedTab.value = tabName },
                        text = { Text(tabName, modifier = Modifier.padding(vertical = 2.dp), fontWeight = if (selectedTab == tabName) FontWeight.Bold else FontWeight.Medium, color = if (selectedTab == tabName) orange600 else Color.Gray) }
                    )
                }
            }

            if (viewModel.isLoading.value) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = orange600) }
            } else {
                LazyColumn(
                    state = listState, // Gắn state để kích hoạt cuộn mượt về đầu trang
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProducts, key = { it.product_id }) { product ->
                        AdminProductItem(
                            product = product,
                            onEditClick = { navController.navigate("admin_edit_product/${product.product_id}") },
                            onDeleteClick = { productToDelete = product }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProductItem(product: Product, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val isOutOfStock = product.quantity == 0
    val orange600 = Color(0xFFEA580C)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                // Lấy tấm ảnh đầu tiên trong mảng List<String> ra làm ảnh đại diện sản phẩm ngoài danh sách
                val img = if (product.images.isNotEmpty()) product.images[0] else ""
                AsyncImage(model = img, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF3F4F6)))

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(formatVND.format(product.price), fontSize = 13.sp, color = orange600, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(if (isOutOfStock) "HẾT HÀNG" else "Kho: ${product.quantity}", fontSize = 12.sp, color = if (isOutOfStock) Color.Red else Color.Gray)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Sửa", tint = orange600, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Xóa", tint = orange600, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB)).padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.End) {
                Text("Đã bán: ${product.sold_count}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
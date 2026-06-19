package com.example.handispace.ui.admin.products

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.handispace.model.Category
import com.example.handispace.ui.components.AppTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditProductScreen(
    navController: NavController,
    productId: String,
    viewModel: AdminProductViewModel = hiltViewModel()
) {
    val context = LocalContext.current // Thêm Context
    val categories = viewModel.categories.value
    val editState = viewModel.editProductState.value
    val orange600 = Color(0xFFEA580C)

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var showCategoryMenu by remember { mutableStateOf(false) }

    // Biến này hứng TẤT CẢ các ảnh: Cả link Firebase cũ lẫn Uri điện thoại mới (dưới dạng String chung)
    var allImages by remember { mutableStateOf<List<String>>(emptyList()) }

    // Gọi bộ chọn ảnh
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val uriStrings = uris.map { it.toString() }
            allImages = allImages + uriStrings
        }
    }

    LaunchedEffect(productId, viewModel.allProducts.value) {
        val product = viewModel.allProducts.value.find { it.product_id == productId }
        if (product != null) {
            name = product.name
            price = product.price.toLong().toString()
            if (price == "0") price = product.price.toString()
            quantity = product.quantity.toString()
            description = product.description
            allImages = product.images // 🔥 Đổ list ảnh cũ từ db vào form
            selectedCategory = categories.find { it.name == product.category_name }
        }
    }

    LaunchedEffect(editState?.data) {
        if (editState?.data == true) {
            viewModel.editProductState.value = null
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh Sửa Sản Phẩm", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Trở lại", tint = orange600)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (editState?.errorMessage != null) {
                        Text(text = editState.errorMessage, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Button(
                        // 🔥 TRUYỀN LIST ẢNH ĐÃ TRỘN VÀO VIEWMODEL
                        onClick = { viewModel.updateProduct(context, productId, name, price, quantity, description, selectedCategory, allImages) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = orange600),
                        enabled = editState?.isLoading != true
                    ) {
                        if (editState?.isLoading == true) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("CẬP NHẬT SẢN PHẨM", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {

            // 🔥 KHU VỰC HIỂN THỊ & SỬA ẢNH
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier.size(100.dp).border(2.dp, Color.LightGray, RoundedCornerShape(8.dp)).background(Color.White, RoundedCornerShape(8.dp)).clickable {
                            multiplePhotoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.AddAPhoto, contentDescription = null, tint = orange600, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Thêm ảnh\n(${allImages.size}/5)", fontSize = 11.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }

                items(allImages) { item ->
                    Box(modifier = Modifier.size(100.dp)) {
                        // AsyncImage chơi láng cả HTTPs mạng lẫn Content Uri máy
                        AsyncImage(model = item, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)))
                        IconButton(
                            onClick = { allImages = allImages.filter { it != item } },
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(50))
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Xóa", tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // FORM NHẬP LIỆU BÊN DƯỚI (Giữ nguyên)
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AppTextField(value = name, onValueChange = { name = it }, label = "Tên sản phẩm", placeholder = "Nhập tên...", modifier = Modifier.fillMaxWidth())
                AppTextField(value = price, onValueChange = { price = it }, label = "Giá bán (VND)", placeholder = "0", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                AppTextField(value = quantity, onValueChange = { quantity = it }, label = "Số lượng", placeholder = "0", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

                Column {
                    Text("Danh mục", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1F2937), modifier = Modifier.padding(bottom = 8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(52.dp).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)).background(Color.White).clickable { showCategoryMenu = true }.padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedCategory?.name ?: "Chọn danh mục...", color = if (selectedCategory != null) Color.Black else Color.Gray)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                        }
                        DropdownMenu(expanded = showCategoryMenu, onDismissRequest = { showCategoryMenu = false }, modifier = Modifier.fillMaxWidth(0.9f)) {
                            categories.forEach { cat -> DropdownMenuItem(text = { Text(cat.name) }, onClick = { selectedCategory = cat; showCategoryMenu = false }) }
                        }
                    }
                }
                AppTextField(value = description, onValueChange = { description = it }, label = "Mô tả sản phẩm", placeholder = "Chi tiết về sản phẩm...", modifier = Modifier.fillMaxWidth().height(120.dp))
            }
        }
    }
}
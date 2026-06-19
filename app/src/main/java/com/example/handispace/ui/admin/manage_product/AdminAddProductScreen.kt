package com.example.handispace.ui.admin.products

import android.net.Uri
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
fun AdminAddProductScreen(
    navController: NavController,
    viewModel: AdminProductViewModel = hiltViewModel()
) {
    val context = LocalContext.current // Lấy context để up ảnh
    val categories = viewModel.categories.value
    val addState = viewModel.addProductState.value
    val orange600 = Color(0xFFEA580C)

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var showCategoryMenu by remember { mutableStateOf(false) }

    // Biến hứng danh sách hình từ điện thoại
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // 🔥 GỌI BỘ CHỌN NHIỀU ẢNH CỦA ANDROID
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5) // Giới hạn 5 ảnh cho mượt
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImages = selectedImages + uris
        }
    }

    LaunchedEffect(addState?.data) {
        if (addState?.data == true) {
            viewModel.addProductState.value = null
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm Sản Phẩm Mới", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                    if (addState?.errorMessage != null) {
                        Text(text = addState.errorMessage, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Button(
                        // 🔥 TRUYỀN LIST ẢNH QUA VIEWMODEL ĐỂ UP
                        onClick = { viewModel.addProduct(context, name, price, quantity, description, selectedCategory, selectedImages) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = orange600),
                        enabled = addState?.isLoading != true
                    ) {
                        if (addState?.isLoading == true) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("ĐĂNG BÁN SẢN PHẨM", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {

            // 🔥 KHU VỰC CHỌN VÀ HIỂN THỊ DANH SÁCH ẢNH
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nút bấm cộng ảnh
                item {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .border(2.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .clickable {
                                multiplePhotoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.AddAPhoto, contentDescription = null, tint = orange600, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Thêm ảnh\n(${selectedImages.size}/5)", fontSize = 11.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }

                // Render list ảnh đang chọn
                items(selectedImages) { uri ->
                    Box(modifier = Modifier.size(100.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                        )
                        // Nút xóa ảnh đỏ chót trên góc
                        IconButton(
                            onClick = { selectedImages = selectedImages.filter { it != uri } },
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
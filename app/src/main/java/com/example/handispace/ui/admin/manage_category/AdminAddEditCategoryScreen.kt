package com.example.handispace.ui.admin.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddEditCategoryScreen(
    navController: NavController,
    categoryId: String?,
    viewModel: AdminCategoryViewModel = hiltViewModel()
) {
    val isEditMode = categoryId != null
    val orange600 = Color(0xFFEA580C)
    val actionState = viewModel.actionState.value

    var inputName by remember { mutableStateOf("") }
    var inputType by remember { mutableStateOf("product") }
    var originalCreatedAt by remember { mutableStateOf<Timestamp?>(null) }

    // 🔥 ĐÃ FIX: Lắng nghe sự thay đổi của danh sách categories.
    // Khi Firebase tải xong data, nó sẽ tự động bơm vào Form.
    LaunchedEffect(categoryId, viewModel.categories.value) {
        if (isEditMode) {
            val cat = viewModel.categories.value.find { it.category_id == categoryId }
            if (cat != null) {
                inputName = cat.name
                inputType = if (cat.type.isNotBlank()) cat.type else "product"
                originalCreatedAt = cat.created_at
            }
        }
    }

    LaunchedEffect(actionState?.data) {
        if (actionState?.data == true) {
            viewModel.clearActionState()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Sửa Danh Mục" else "Thêm Danh Mục Mới", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                    if (actionState?.errorMessage != null) {
                        Text(text = actionState.errorMessage, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Button(
                        onClick = {
                            if (isEditMode) {
                                viewModel.updateCategory(categoryId!!, inputName, inputType, originalCreatedAt)
                            } else {
                                viewModel.addCategory(inputName, inputType)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = orange600),
                        enabled = inputName.isNotBlank() && actionState?.isLoading != true
                    ) {
                        if (actionState?.isLoading == true) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("LƯU DANH MỤC", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            OutlinedTextField(
                value = inputName,
                onValueChange = { inputName = it },
                label = { Text("Tên danh mục", color = Color.Gray) },
                placeholder = { Text("VD: Đất sét tự khô...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = orange600,
                    focusedLabelColor = orange600
                )
            )

            Column(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(16.dp)) {
                Text("Loại danh mục:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1F2937))
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    RadioButton(
                        selected = inputType == "product",
                        onClick = { inputType = "product" },
                        colors = RadioButtonDefaults.colors(selectedColor = orange600)
                    )
                    Text("Sản phẩm (Bán thành phẩm)", fontSize = 14.sp, modifier = Modifier.padding(end = 16.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    RadioButton(
                        selected = inputType == "material",
                        onClick = { inputType = "material" },
                        colors = RadioButtonDefaults.colors(selectedColor = orange600)
                    )
                    Text("Vật liệu (Nguyên liệu làm handmade)", fontSize = 14.sp)
                }
            }
        }
    }
}
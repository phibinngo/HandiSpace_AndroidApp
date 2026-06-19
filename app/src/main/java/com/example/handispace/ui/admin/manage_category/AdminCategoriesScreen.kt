package com.example.handispace.ui.admin.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
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
import com.example.handispace.model.Category
import com.example.handispace.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoriesScreen(
    navController: NavController,
    viewModel: AdminCategoryViewModel = hiltViewModel()
) {
    val processedList = viewModel.getProcessedCategories()
    val searchQuery = viewModel.searchQuery.value
    val sortOption = viewModel.sortOption.value
    val isLoading = viewModel.isLoading.value

    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)

    var showSortMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<Category?>(null) }

    // Popup Xác nhận Xóa
    showDeleteConfirmDialog?.let { cat ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Xác nhận xóa", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa danh mục \"${cat.name}\" không? \n(Chỉ nên xóa khi danh mục không có sản phẩm nào)") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteCategory(cat.category_id); showDeleteConfirmDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Xóa") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = null }) { Text("Hủy", color = Color.Gray) }
            },
            containerColor = Color.White
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.ADMIN_ADD_CATEGORY) },
                containerColor = orange600,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) { Icon(Icons.Filled.Add, contentDescription = "Thêm danh mục") }
        },
        containerColor = gray50
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // THANH TÌM KIẾM
            Box(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Tìm kiếm danh mục...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = orange600) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = orange600,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = gray50,
                        unfocusedContainerColor = gray50
                    )
                )
            }

            // DÒNG SẮP XẾP
            Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Tổng: ${processedList.size} danh mục", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
                Box {
                    TextButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Filled.Sort, contentDescription = null, tint = orange600, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(sortOption, color = orange600, fontSize = 13.sp)
                    }
                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        listOf("Tên A-Z", "Hàng hóa nhiều nhất", "Hàng hóa ít nhất").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = if (sortOption == option) orange600 else Color.Black) },
                                onClick = { viewModel.sortOption.value = option; showSortMenu = false }
                            )
                        }
                    }
                }
            }

            // DANH SÁCH
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = orange600) }
            } else if (processedList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Không tìm thấy danh mục nào", color = Color.Gray) }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(processedList, key = { it.category.category_id }) { item ->
                        val cat = item.category
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.size(48.dp).background(Color(0xFFFFF7ED), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Category, contentDescription = null, tint = orange600)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(cat.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(if (cat.type == "product") "Sản phẩm" else "Vật liệu", fontSize = 12.sp, color = orange600, modifier = Modifier.background(Color(0xFFFFEDD5), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Kho: ${item.productCount} SP", fontSize = 13.sp, color = Color.Gray)
                                        }
                                    }
                                }

                                // Nút Sửa / Xóa
                                Row {
                                    IconButton(
                                        onClick = { navController.navigate("${Routes.ADMIN_EDIT_CATEGORY}/${cat.category_id}") },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Sửa", tint = orange600, modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = { showDeleteConfirmDialog = cat }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Xóa", tint = orange600, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
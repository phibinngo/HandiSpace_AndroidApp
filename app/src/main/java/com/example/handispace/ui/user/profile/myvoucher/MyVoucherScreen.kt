package com.example.handispace.ui.user.profile.myvoucher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.handispace.navigation.Routes
import com.example.handispace.ui.components.VoucherCardDisplay
import com.example.handispace.ui.components.getSharedVoucherCategoryLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyVoucherScreen(navController: NavController, viewModel: MyVoucherViewModel) {
    val state = viewModel.myVouchersState.value
    var filterType by remember { mutableStateOf("all") }

    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilters by remember { mutableStateOf<Set<String>>(emptySet()) }

    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)
    val safeVouchers = state.data ?: emptyList()

    val orderVouchers = safeVouchers.filter { it.type == "order" }
    val availableCategories = remember(orderVouchers) {
        val cats = orderVouchers.map { getSharedVoucherCategoryLabel(it) }.distinct().toMutableList()
        if (cats.contains("TOÀN SÀN")) {
            cats.remove("TOÀN SÀN")
            cats.add(0, "TOÀN SÀN")
        }
        cats
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kho Voucher của tôi", fontSize = 18.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Trở lại", tint = orange600)
                    }
                },
                actions = {
                    TextButton(onClick = { navController.navigate(Routes.VOUCHER_HISTORY) }) {
                        Text("Lịch sử", color = orange600, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = gray50
    ) { padding ->

        if (showFilterDialog) {
            MyVoucherFilterDialog(
                categories = availableCategories,
                initialSelectedCats = selectedCategoryFilters,
                onDismiss = { showFilterDialog = false },
                onApply = { selectedCats ->
                    selectedCategoryFilters = selectedCats
                    showFilterDialog = false
                }
            )
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChipVoucher("Tất cả", filterType == "all") { filterType = "all" }
                    FilterChipVoucher("Miễn Phí Vận Chuyển", filterType == "shipping") { filterType = "shipping" }
                    FilterChipVoucher("Giảm Giá", filterType == "order") { filterType = "order" }
                }

                IconButton(onClick = { showFilterDialog = true }, modifier = Modifier.size(32.dp).padding(start = 8.dp)) {
                    Icon(Icons.Default.Menu, contentDescription = "Lọc", tint = orange600)
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = orange600) }
            } else if (safeVouchers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Bạn chưa lưu Voucher nào.", color = Color.Gray) }
            } else {
                val displayList = safeVouchers.filter { voucher ->
                    val matchType = if (filterType == "all") true else voucher.type == filterType
                    val matchCategory = if (selectedCategoryFilters.isEmpty() || voucher.type == "shipping") {
                        true
                    } else {
                        selectedCategoryFilters.contains(getSharedVoucherCategoryLabel(voucher))
                    }
                    matchType && matchCategory
                }

                if (displayList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) { Text("Không có voucher nào phù hợp.", color = Color.Gray) }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(displayList) { voucher ->
                            VoucherCardDisplay(
                                voucher = voucher,
                                buttonText = "Dùng Ngay",
                                onButtonClick = { navController.navigate(Routes.HOME) { popUpTo(0) } }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherHistoryScreen(navController: NavController, viewModel: MyVoucherViewModel) {
    val state = viewModel.historyVouchersState.value
    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử Voucher", fontSize = 18.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, contentDescription = "Trở lại", tint = orange600) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = gray50
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val historyList = state.data ?: emptyList()

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = orange600) }
            } else if (historyList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Bạn chưa sử dụng Voucher nào.", color = Color.Gray) }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(historyList) { voucher ->
                        VoucherCardDisplay(
                            voucher = voucher,
                            buttonText = "Đã sử dụng",
                            isButtonEnabled = false,
                            isGrayOutButton = true,
                            onButtonClick = {}
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipVoucher(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(if (isSelected) Color(0xFFFFF7ED) else Color(0xFFF3F4F6)).border(1.dp, if (isSelected) Color(0xFFEA580C) else Color.Transparent, RoundedCornerShape(16.dp)).clickable { onClick() }.padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
        Text(text, color = if (isSelected) Color(0xFFEA580C) else Color(0xFF4B5563), fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MyVoucherFilterDialog(
    categories: List<String>,
    initialSelectedCats: Set<String>,
    onDismiss: () -> Unit,
    onApply: (Set<String>) -> Unit
) {
    var tempSelectedCats by remember { mutableStateOf(initialSelectedCats) }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    val orange600 = Color(0xFFEA580C)
    val orange50 = Color(0xFFFFF7ED)
    val gray100 = Color(0xFFF3F4F6)
    val gray300 = Color(0xFFD1D5DB)
    val gray800 = Color(0xFF1F2937)

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().background(gray100).padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("PHÂN LOẠI VOUCHER", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = gray800)
                }

                Column(modifier = Modifier.fillMaxWidth().weight(1f, fill = false).padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Danh mục áp dụng", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = gray800)
                    Spacer(modifier = Modifier.height(12.dp))

                    val itemsToDisplay = mutableListOf<String>()
                    if (categories.size <= 6) {
                        itemsToDisplay.addAll(categories)
                    } else {
                        if (isCategoryExpanded) {
                            itemsToDisplay.addAll(categories)
                            itemsToDisplay.add("LESS_BTN")
                        } else {
                            itemsToDisplay.addAll(categories.take(5))
                            itemsToDisplay.add("MORE_BTN")
                        }
                    }

                    val chunkedCategories = itemsToDisplay.chunked(2)
                    chunkedCategories.forEach { rowCats ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowCats.forEach { catName ->
                                if (catName == "MORE_BTN") {
                                    Box(modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(6.dp)).background(gray100).clickable { isCategoryExpanded = true }, contentAlignment = Alignment.Center) { Text("Xem thêm ▼", fontSize = 13.sp, color = gray800, fontWeight = FontWeight.Medium) }
                                } else if (catName == "LESS_BTN") {
                                    Box(modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(6.dp)).background(gray100).clickable { isCategoryExpanded = false }, contentAlignment = Alignment.Center) { Text("Rút gọn ▲", fontSize = 13.sp, color = gray800, fontWeight = FontWeight.Medium) }
                                } else {
                                    val isSelected = tempSelectedCats.contains(catName)
                                    Box(
                                        modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) orange50 else gray100).border(1.dp, if (isSelected) orange600 else Color.Transparent, RoundedCornerShape(6.dp))
                                            .clickable { tempSelectedCats = if (isSelected) tempSelectedCats - catName else tempSelectedCats + catName },
                                        contentAlignment = Alignment.Center
                                    ) { Text(catName, fontSize = 13.sp, color = if (isSelected) orange600 else gray800, textAlign = TextAlign.Center, maxLines = 1) }
                                }
                            }
                            if (rowCats.size == 1) Spacer(modifier = Modifier.weight(1f).height(44.dp).background(Color.Transparent))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { tempSelectedCats = emptySet(); isCategoryExpanded = false }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, gray300)) { Text("Thiết lập lại", color = gray800, fontWeight = FontWeight.Medium) }
                    Button(onClick = { onApply(tempSelectedCats) }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = orange600)) { Text("Áp dụng", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
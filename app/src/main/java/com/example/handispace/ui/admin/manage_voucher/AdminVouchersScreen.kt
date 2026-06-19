package com.example.handispace.ui.admin.vouchers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign // Đã import TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.handispace.model.Voucher
import com.example.handispace.navigation.Routes
import com.example.handispace.ui.components.AppTopBar
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AdminVouchersScreen(
    navController: NavController,
    viewModel: AdminVoucherViewModel = hiltViewModel()
) {
    val selectedTab = viewModel.selectedTab.value
    val filteredVouchers = viewModel.getFilteredVouchers()
    val isLoading = viewModel.isLoading.value

    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)
    var voucherToDelete by remember { mutableStateOf<Voucher?>(null) }

    voucherToDelete?.let { voucher ->
        AlertDialog(
            onDismissRequest = { voucherToDelete = null },
            title = { Text("Xác nhận xóa voucher", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có thực sự muốn gỡ bỏ mã giảm giá \"${voucher.code}\" khỏi cơ sở dữ liệu?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = { viewModel.deleteVoucher(voucher.voucher_id); voucherToDelete = null }
                ) { Text("Xóa bỏ") }
            },
            dismissButton = {
                OutlinedButton(onClick = { voucherToDelete = null }) { Text("Hủy", color = Color.Gray) }
            },
            containerColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Quản lý Voucher",
                onBackClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            if (selectedTab != "Freeship") {
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.ADMIN_ADD_VOUCHER) },
                    containerColor = orange600,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) { Icon(Icons.Filled.Add, contentDescription = "Thêm voucher") }
            }
        },
        containerColor = gray50
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = viewModel.tabs.indexOf(selectedTab).coerceAtLeast(0),
                containerColor = Color.White,
                contentColor = orange600,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    if (tabPositions.isNotEmpty()) {
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[viewModel.tabs.indexOf(selectedTab).coerceAtLeast(0)]),
                            color = orange600, height = 3.dp
                        )
                    }
                }
            ) {
                viewModel.tabs.forEach { tabName ->
                    Tab(
                        selected = selectedTab == tabName,
                        onClick = { viewModel.selectedTab.value = tabName },
                        text = { Text(tabName, fontWeight = if (selectedTab == tabName) FontWeight.Bold else FontWeight.Medium, color = if (selectedTab == tabName) orange600 else Color.Gray) }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = orange600) }
            } else if (filteredVouchers.isEmpty() && selectedTab != "Freeship") {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Không tìm thấy voucher nào thuộc nhóm này", color = Color.Gray) }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    if (selectedTab == "Freeship") {
                        item {
                            FixedFreeshipCard()
                        }
                    }

                    items(filteredVouchers, key = { it.voucher_id }) { voucher ->
                        AdminVoucherCardItem(
                            voucher = voucher,
                            onEditClick = { navController.navigate("${Routes.ADMIN_EDIT_VOUCHER}/${voucher.voucher_id}") },
                            onDeleteClick = { voucherToDelete = voucher }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminVoucherCardItem(voucher: Voucher, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val orange600 = Color(0xFFEA580C) // 🔥 ĐÃ THÊM BIẾN NÀY ĐỂ TRÁNH LỖI UNRESOLVED REFERENCE
    val isFreeship = voucher.type == "shipping"
    val mainColor = if (isFreeship) Color(0xFF0FA49C) else orange600

    val labelText = if (voucher.type == "shipping") "FREESHIP"
    else if (voucher.applicable_categories.isEmpty()) "TOÀN SÀN"
    else voucher.applicable_category_names.firstOrNull()?.uppercase() ?: "DANH MỤC"

    val discountText = if (voucher.discount_type == "percent") {
        if (voucher.max_discount > 0) "Giảm ${voucher.discount_value.toInt()}% (Tối đa ${formatVND.format(voucher.max_discount).replace("₫", "K")})"
        else "Giảm ${voucher.discount_value.toInt()}%"
    } else {
        "Giảm ${formatVND.format(voucher.discount_value).replace("₫", "K")}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(mainColor, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(labelText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }

        Box(modifier = Modifier.weight(2.5f).fillMaxHeight().padding(12.dp)) {
            Column(modifier = Modifier.fillMaxWidth().align(Alignment.TopStart)) {
                Text(
                    text = discountText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1F2937),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Đơn tối thiểu ${formatVND.format(voucher.min_order_value)}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val limitText = if (voucher.usage_limit <= 0) "Vô hạn" else voucher.usage_limit.toString()
                Text(text = "Đã dùng: ${voucher.used_count} / $limitText", fontSize = 11.sp, color = Color.DarkGray)
            }

            Row(modifier = Modifier.align(Alignment.BottomEnd)) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Sửa", tint = orange600, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Xóa", tint = orange600, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun FixedFreeshipCard() {
    val mainColor = Color(0xFF0FA49C)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(mainColor, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
                Text("FREESHIP\nTOÀN SÀN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }

        Box(modifier = Modifier.weight(2.5f).fillMaxHeight().padding(12.dp)) {
            Column(modifier = Modifier.fillMaxWidth().align(Alignment.TopStart)) {
                Text(
                    text = "Giảm tối đa 30K",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1F2937),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Đơn tối thiểu 100K",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
                Text(text = "Phát hành bởi: Hệ Thống", fontSize = 11.sp, color = mainColor, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
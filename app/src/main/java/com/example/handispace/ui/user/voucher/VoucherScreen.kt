package com.example.handispace.ui.user.voucher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.handispace.ui.components.ErrorPopup
import com.example.handispace.ui.components.SuccessPopup
import com.example.handispace.ui.components.VoucherCardDisplay
import com.example.handispace.ui.components.getSharedVoucherCategoryLabel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherScreen(viewModel: UserVoucherViewModel) {
    val state = viewModel.vouchersState.value
    val savedIds = viewModel.savedVoucherIds.value
    val usedIds = viewModel.usedVoucherIds.value
    val coroutineScope = rememberCoroutineScope()

    var inputCode by remember { mutableStateOf("") }

    var showSuccess by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var popupMsg by remember { mutableStateOf("") }

    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilters by remember { mutableStateOf<Set<String>>(emptySet()) }

    val shippingVouchers = state.data?.filter { it.type == "shipping" } ?: emptyList()

    // 🔥 ẨN LUÔN VOUCHER ĐÃ DÙNG KHỎI MÀN HÌNH CHÍNH
    val orderVouchers = state.data?.filter { it.type == "order" && !usedIds.contains(it.voucher_id) } ?: emptyList()

    val availableCategories = remember(orderVouchers) {
        val cats = orderVouchers.map { getSharedVoucherCategoryLabel(it) }.distinct().toMutableList()
        if (cats.contains("TOÀN SÀN")) {
            cats.remove("TOÀN SÀN")
            cats.add(0, "TOÀN SÀN")
        }
        cats
    }

    val filteredOrderVouchers = if (selectedCategoryFilters.isEmpty()) {
        orderVouchers
    } else {
        orderVouchers.filter { voucher ->
            selectedCategoryFilters.contains(getSharedVoucherCategoryLabel(voucher))
        }
    }

    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp, color = Color.White) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = inputCode, onValueChange = { inputCode = it.uppercase() }, placeholder = { Text("Nhập mã giảm giá...", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f).height(50.dp), singleLine = true, shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = orange600, unfocusedBorderColor = Color.LightGray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (inputCode.isNotBlank()) {
                                viewModel.applyCode(inputCode) { isSuccess, msg ->
                                    popupMsg = msg
                                    if (isSuccess) { showSuccess = true; inputCode = ""; coroutineScope.launch { delay(1500); showSuccess = false } } else { showError = true }
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = orange600), modifier = Modifier.height(50.dp)
                    ) { Text("ÁP DỤNG", fontWeight = FontWeight.Bold) }
                }
            }
        },
        containerColor = gray50
    ) { padding ->

        if (showSuccess) SuccessPopup(message = popupMsg) { showSuccess = false }
        if (showError) ErrorPopup(errorMessage = popupMsg) { showError = false }

        if (showFilterDialog) {
            VoucherFilterDialog(
                categories = availableCategories,
                initialSelectedCats = selectedCategoryFilters,
                onDismiss = { showFilterDialog = false },
                onApply = { selectedCats ->
                    selectedCategoryFilters = selectedCats
                    showFilterDialog = false
                }
            )
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = orange600) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {

                if (shippingVouchers.isNotEmpty()) {
                    item {
                        Text("Miễn Phí Vận Chuyển", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937), modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                    }
                    items(shippingVouchers) { voucher ->
                        val isSaved = savedIds.contains(voucher.voucher_id)

                        VoucherCardDisplay(
                            voucher = voucher,
                            buttonText = if (isSaved) "Đã lưu" else "Lưu",
                            isButtonEnabled = !isSaved,
                            isGrayOutButton = isSaved,
                            onButtonClick = {
                                if (!isSaved) {
                                    viewModel.saveVoucher(voucher.voucher_id) { isSuccess, msg ->
                                        popupMsg = msg
                                        if (isSuccess) { showSuccess = true; coroutineScope.launch { delay(1500); showSuccess = false } } else { showError = true }
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                if (orderVouchers.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mã Giảm Giá Đơn Hàng", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))

                            IconButton(onClick = { showFilterDialog = true }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Menu, contentDescription = "Lọc", tint = orange600)
                            }
                        }
                    }

                    if (filteredOrderVouchers.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                                Text("Chưa có mã giảm giá cho danh mục này.", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    } else {
                        items(filteredOrderVouchers) { voucher ->
                            // 🔥 ĐÃ RÚT GỌN LOGIC BUTTON (Vì danh sách này chắc chắn chưa dùng)
                            val isSaved = savedIds.contains(voucher.voucher_id)

                            VoucherCardDisplay(
                                voucher = voucher,
                                buttonText = if (isSaved) "Đã lưu" else "Lưu",
                                isButtonEnabled = !isSaved,
                                isGrayOutButton = isSaved,
                                onButtonClick = {
                                    if (!isSaved) {
                                        viewModel.saveVoucher(voucher.voucher_id) { isSuccess, msg ->
                                            popupMsg = msg
                                            if (isSuccess) { showSuccess = true; coroutineScope.launch { delay(1500); showSuccess = false } } else { showError = true }
                                        }
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

// ... (Giữ nguyên phần VoucherFilterDialog bên dưới của ní) ...
@Composable
fun VoucherFilterDialog(
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
        Card(
            shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)
        ) {
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
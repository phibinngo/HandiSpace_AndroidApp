package com.example.handispace.ui.user.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.handispace.navigation.Routes
import com.example.handispace.ui.components.ErrorPopup
import com.example.handispace.ui.components.SuccessPopup
import com.example.handispace.ui.components.SharedVoucherBottomSheet
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController, viewModel: CartViewModel) {
    val state = viewModel.cartState.value
    val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)
    val gray300 = Color(0xFFD1D5DB)
    val gray500 = Color(0xFF6B7280)
    val gray800 = Color(0xFF1F2937)

    var selectedItemIds by remember { mutableStateOf(setOf<String>()) }
    var itemToDelete by remember { mutableStateOf<String?>(null) }
    var showVoucherSheet by remember { mutableStateOf(false) }

    val cartItems = state.data?.items ?: emptyList()
    val isAllSelected = cartItems.isNotEmpty() && selectedItemIds.size == cartItems.size

    val totalPrice = cartItems
        .filter { it.product_id in selectedItemIds }
        .sumOf { it.price * it.quantity }

    val freeshipVoucher = viewModel.selectedFreeshipVoucher.value
    val discountVoucher = viewModel.selectedDiscountVoucher.value
    val shipReduction = if (freeshipVoucher != null) viewModel.calculateDiscountValue(freeshipVoucher, totalPrice) else 0.0
    val itemReduction = if (discountVoucher != null) viewModel.calculateDiscountValue(discountVoucher, totalPrice) else 0.0

    var showErrorPopup by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccessPopup by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    LaunchedEffect(showErrorPopup) { if (showErrorPopup) { delay(1000); showErrorPopup = false } }
    LaunchedEffect(showSuccessPopup) { if (showSuccessPopup) { delay(1000); showSuccessPopup = false } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giỏ hàng (${cartItems.size})", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = gray800) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Trở lại", tint = orange600)
                    }
                },
                actions = {
                    if (selectedItemIds.isNotEmpty()) {
                        TextButton(onClick = {
                            selectedItemIds.forEach { id -> viewModel.removeItem(id) }
                            selectedItemIds = emptySet()
                            successMessage = "Đã xóa các sản phẩm được chọn"
                            showSuccessPopup = true
                        }) { Text("Xóa", color = orange600, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Column(modifier = Modifier.background(Color.White)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showVoucherSheet = true }.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.ConfirmationNumber, contentDescription = "Voucher", tint = orange600, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("HandiSpace Voucher", fontSize = 14.sp, color = gray800, modifier = Modifier.weight(1f))

                        if (shipReduction > 0 || itemReduction > 0) {
                            Column(horizontalAlignment = Alignment.End) {
                                if (shipReduction > 0) Text("-${formatVND.format(shipReduction).replace("₫", "K")}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0FA49C))
                                if (itemReduction > 0) Text("-${formatVND.format(itemReduction).replace("₫", "K")}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = orange600)
                            }
                        } else {
                            Text("Chọn hoặc nhập mã", fontSize = 12.sp, color = gray500)
                        }

                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Chọn", tint = gray500)
                    }

                    Divider(color = gray50, thickness = 1.dp)

                    Row(modifier = Modifier.fillMaxWidth().height(60.dp), verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedItemIds = if (isAllSelected) emptySet() else cartItems.map { it.product_id }.toSet() }.padding(start = 12.dp, end = 8.dp).fillMaxHeight()
                        ) {
                            Checkbox(checked = isAllSelected, onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = orange600, uncheckedColor = gray300))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tất cả", fontSize = 14.sp, color = gray800)
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 12.dp)) {
                            val finalPrice = (totalPrice - itemReduction).coerceAtLeast(0.0)
                            Text(
                                text = formatVND.format(finalPrice),
                                fontSize = if (finalPrice >= 10000000) 14.sp else 16.sp,
                                fontWeight = FontWeight.Bold, color = orange600, maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box(
                            modifier = Modifier.width(110.dp).fillMaxHeight().background(if (selectedItemIds.isNotEmpty()) orange600 else gray300)
                                .clickable(enabled = selectedItemIds.isNotEmpty()) {
                                    val itemIdsParam = selectedItemIds.joinToString(",")
                                    val shipIdParam = viewModel.selectedFreeshipVoucher.value?.voucher_id?.takeIf { it.isNotBlank() } ?: "none"
                                    val discountIdParam = viewModel.selectedDiscountVoucher.value?.voucher_id?.takeIf { it.isNotBlank() } ?: "none"
                                    navController.navigate("${Routes.CHECKOUT}/$itemIdsParam/$shipIdParam/$discountIdParam")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Mua Hàng (${selectedItemIds.size})", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        },
        containerColor = gray50
    ) { padding ->

        if (showErrorPopup) ErrorPopup(errorMessage = errorMessage, onDismiss = { showErrorPopup = false })
        if (showSuccessPopup) SuccessPopup(message = successMessage, onDismiss = { showSuccessPopup = false })

        if (itemToDelete != null) {
            com.example.handispace.ui.components.ConfirmDeletePopup(
                onConfirm = {
                    viewModel.removeItem(itemToDelete!!)
                    selectedItemIds = selectedItemIds - itemToDelete!!
                    itemToDelete = null
                    successMessage = "Đã xóa sản phẩm"
                    showSuccessPopup = true
                },
                onCancel = { itemToDelete = null }
            )
        }

        if (showVoucherSheet) {
            SharedVoucherBottomSheet(
                vouchers = viewModel.myVouchers.value,
                selectedFreeshipId = viewModel.selectedFreeshipVoucher.value?.voucher_id,
                selectedDiscountId = viewModel.selectedDiscountVoucher.value?.voucher_id,
                isEligible = { v -> totalPrice >= v.min_order_value && viewModel.isVoucherApplicable(v, selectedItemIds) },
                onApplyCode = { code, callback -> viewModel.applyVoucherByCode(code, totalPrice, selectedItemIds, callback) },
                onAutoSelect = { viewModel.autoSelectBestVouchers(totalPrice, selectedItemIds) },
                onSelectFreeship = { v -> viewModel.selectedFreeshipVoucher.value = v },
                onSelectDiscount = { v -> viewModel.selectedDiscountVoucher.value = v },
                onDismiss = { showVoucherSheet = false }
            )
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = orange600) }
        } else if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Giỏ hàng đang trống", color = gray500) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)) {
                items(cartItems) { item ->
                    val isOutOfStock = item.max_stock == 0
                    val isSelected = item.product_id in selectedItemIds

                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp).background(Color.White)) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked -> selectedItemIds = if (checked) selectedItemIds + item.product_id else selectedItemIds - item.product_id },
                                colors = CheckboxDefaults.colors(checkedColor = orange600, uncheckedColor = gray300)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Box {
                                AsyncImage(
                                    model = item.image, contentDescription = null, contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(80.dp).border(1.dp, gray50, RoundedCornerShape(4.dp)).clip(RoundedCornerShape(4.dp)).background(gray50)
                                        .clickable { navController.navigate("product_detail/${item.product_id}") }
                                )
                                if (isOutOfStock) {
                                    Box(modifier = Modifier.size(80.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                                        Text("HẾT HÀNG", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.name, fontSize = 14.sp, color = if (isOutOfStock) gray500 else gray800, maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.clickable { navController.navigate("product_detail/${item.product_id}") }
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(formatVND.format(item.price), fontSize = 15.sp, fontWeight = FontWeight.Medium, color = if (isOutOfStock) gray500 else orange600)

                                    if (!isOutOfStock) {
                                        Row(modifier = Modifier.border(1.dp, gray300, RoundedCornerShape(4.dp)).height(28.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier.width(28.dp).fillMaxHeight().clickable {
                                                    if (item.quantity > 1) viewModel.changeQuantity(item.product_id, false) else itemToDelete = item.product_id
                                                },
                                                contentAlignment = Alignment.Center
                                            ) { Text("-", color = gray500, fontSize = 14.sp) }

                                            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(gray300))
                                            Box(modifier = Modifier.width(40.dp).fillMaxHeight(), contentAlignment = Alignment.Center) { Text(item.quantity.toString(), color = gray800, fontSize = 13.sp) }
                                            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(gray300))

                                            Box(
                                                modifier = Modifier.width(28.dp).fillMaxHeight().clickable {
                                                    if (item.quantity < item.max_stock) viewModel.changeQuantity(item.product_id, true)
                                                    else { errorMessage = "Số lượng hàng còn lại là ${item.max_stock}"; showErrorPopup = true }
                                                },
                                                contentAlignment = Alignment.Center
                                            ) { Text("+", color = gray500, fontSize = 14.sp) }
                                        }
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                                    Text(
                                        text = "Xóa", color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                                        modifier = Modifier.clickable { itemToDelete = item.product_id }.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    Divider(color = gray50, thickness = 1.dp)
                }
            }
        }
    }
}
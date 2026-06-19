package com.example.handispace.ui.user.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
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
fun CheckoutScreen(
    navController: NavController,
    viewModel: CheckoutViewModel,
    selectedItemIds: List<String>,
    freeshipVoucherId: String?,
    discountVoucherId: String?
) {
    val state = viewModel.state.value
    val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    var note by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("COD") }
    var showVoucherSheet by remember { mutableStateOf(false) }

    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)
    val gray500 = Color(0xFF6B7280)
    val gray800 = Color(0xFF1F2937)

    val returnedAddressId = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_address_id")

    LaunchedEffect(returnedAddressId) {
        if (returnedAddressId != null) {
            viewModel.selectAddress(returnedAddressId)
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_address_id")
        }
    }

    LaunchedEffect(Unit) {
        if (returnedAddressId == null) {
            viewModel.prepareCheckoutData(selectedItemIds, freeshipVoucherId, discountVoucherId)
        }
    }

    if (state.isSuccess) {
        LaunchedEffect(Unit) {
            delay(2000)
            viewModel.clearMessage()
            navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
        }
        SuccessPopup(
            message = "Đặt hàng thành công!",
            onDismiss = {
                viewModel.clearMessage()
                navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
            }
        )
    }

    if (state.errorMessage != null) {
        LaunchedEffect(state.errorMessage) {
            delay(1500)
            viewModel.clearMessage()
        }
        ErrorPopup(
            errorMessage = state.errorMessage,
            onDismiss = { viewModel.clearMessage() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, contentDescription = null, tint = orange600) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                        Text("Tổng thanh toán", fontSize = 13.sp, color = gray800)
                        Text(formatVND.format(state.finalTotal), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = orange600)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { viewModel.placeOrder(note, paymentMethod) },
                        modifier = Modifier.width(150.dp).height(48.dp),
                        shape = RoundedCornerShape(4.dp), colors = ButtonDefaults.buttonColors(containerColor = orange600), enabled = !state.isLoading
                    ) {
                        if (state.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        else Text("Đặt hàng", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        },
        containerColor = gray50
    ) { padding ->

        if (showVoucherSheet) {
            SharedVoucherBottomSheet(
                vouchers = viewModel.myVouchers.value,
                selectedFreeshipId = viewModel.selectedFreeshipVoucher.value?.voucher_id,
                selectedDiscountId = viewModel.selectedDiscountVoucher.value?.voucher_id,
                isEligible = { v -> state.subtotal >= v.min_order_value && viewModel.isVoucherApplicable(v) },
                onApplyCode = { code, onResult -> viewModel.applyVoucherByCode(code, onResult) },
                onAutoSelect = { viewModel.autoSelectBestVouchers() },
                onSelectFreeship = { v -> viewModel.selectFreeshipVoucher(v) },
                onSelectDiscount = { v -> viewModel.selectDiscountVoucher(v) },
                onDismiss = { showVoucherSheet = false }
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().background(Color.White).clickable {
                        navController.navigate("address_selection")
                    }.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = orange600, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Địa chỉ nhận hàng", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = gray800)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = gray500)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    if (state.defaultAddress != null) {
                        Text("${state.defaultAddress.receiver_name} | ${state.defaultAddress.receiver_phone}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("${state.defaultAddress.street_address}, ${state.defaultAddress.ward}, ${state.defaultAddress.province}", fontSize = 13.sp, color = gray500)
                    } else {
                        Text("Chưa có địa chỉ. Bấm vào đây để thêm!", color = Color.Red, fontSize = 14.sp)
                    }
                }
                Divider(color = gray50, thickness = 8.dp)
            }

            items(state.checkoutItems) { item ->
                Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = item.image, contentDescription = null, modifier = Modifier.size(60.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Phân loại: Tùy chọn", fontSize = 12.sp, color = gray500)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(formatVND.format(item.price), color = orange600, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("x${item.quantity}", fontSize = 14.sp)
                        }
                    }
                }
                Divider(color = gray50, thickness = 1.dp)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tổng số tiền (${state.checkoutItems.sumOf { it.quantity }} sản phẩm): ", fontSize = 14.sp, color = gray800)
                    Text(formatVND.format(state.subtotal), fontSize = 15.sp, color = orange600, fontWeight = FontWeight.Medium)
                }
                Divider(color = gray50, thickness = 8.dp)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.White).clickable { showVoucherSheet = true }.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.ConfirmationNumber, contentDescription = "Voucher", tint = orange600, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Voucher của Shop", fontSize = 14.sp, color = gray800, modifier = Modifier.weight(1f))

                    if (state.freeshipDiscount > 0 || state.orderDiscount > 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            if (state.freeshipDiscount > 0) Text("-${formatVND.format(state.freeshipDiscount).replace("₫", "K")}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0FA49C))
                            if (state.orderDiscount > 0) Text("-${formatVND.format(state.orderDiscount).replace("₫", "K")}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = orange600)
                        }
                    } else {
                        Text("Chọn hoặc nhập mã", fontSize = 13.sp, color = gray500)
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Chọn", tint = gray500)
                }
                Divider(color = gray50, thickness = 8.dp)
            }

            item {
                Column(modifier = Modifier.background(Color.White).padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Lời nhắn:", fontSize = 14.sp, color = gray800)
                        Spacer(modifier = Modifier.width(12.dp))
                        TextField(
                            value = note, onValueChange = { note = it }, placeholder = { Text("Lưu ý cho người bán...", fontSize = 14.sp) },
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Divider(color = gray50, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                        Text("Phí vận chuyển (Mặc định)", fontSize = 14.sp, color = gray500)
                        Text(formatVND.format(state.shippingFee), fontSize = 14.sp, color = gray800)
                    }
                }
                Divider(color = gray50, thickness = 8.dp)
            }

            item {
                Column(modifier = Modifier.background(Color.White).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = orange600, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Phương thức thanh toán", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { paymentMethod = "COD" }.fillMaxWidth()) {
                        RadioButton(selected = paymentMethod == "COD", onClick = { paymentMethod = "COD" }, colors = RadioButtonDefaults.colors(selectedColor = orange600))
                        Text("Thanh toán khi nhận hàng (COD)", fontSize = 14.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { paymentMethod = "BANK_TRANSFER" }.fillMaxWidth()) {
                        RadioButton(selected = paymentMethod == "BANK_TRANSFER", onClick = { paymentMethod = "BANK_TRANSFER" }, colors = RadioButtonDefaults.colors(selectedColor = orange600))
                        Text("Chuyển khoản ngân hàng", fontSize = 14.sp)
                    }
                }
                Divider(color = gray50, thickness = 8.dp)
            }

            // ... (Giữ nguyên các thẻ import và phần trên của file) ...

            item {
                Column(modifier = Modifier.background(Color.White).padding(16.dp)) {
                    Text("\uD83D\uDCC4 Chi tiết thanh toán", fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                    PaymentDetailRow("Tổng tiền hàng", formatVND.format(state.subtotal))

                    // 🔥 HIỂN THỊ RÕ TÊN HẠNG VÀ % TRÊN UI CHECKOUT
                    if (state.memberDiscount > 0) {
                        val percent = state.loyaltyDiscountPercent.toInt()
                        val rankName = state.loyaltyLevelName
                        PaymentDetailRow("Ưu đãi Thành viên $rankName ($percent%)", "-${formatVND.format(state.memberDiscount)}", orange600)
                    }

                    if (state.orderDiscount > 0) PaymentDetailRow("Voucher giảm giá đơn", "-${formatVND.format(state.orderDiscount)}", orange600)
                    PaymentDetailRow("Tổng tiền phí vận chuyển", formatVND.format(state.shippingFee))
                    if (state.freeshipDiscount > 0) PaymentDetailRow("Giảm giá phí vận chuyển", "-${formatVND.format(state.freeshipDiscount)}", Color(0xFF0FA49C))

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Tổng thanh toán", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = gray800)
                        Text(formatVND.format(state.finalTotal), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = orange600)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ... (Giữ nguyên hàm PaymentDetailRow) ...

@Composable
fun PaymentDetailRow(label: String, value: String, valueColor: Color = Color(0xFF1F2937)) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = Color(0xFF6B7280))
        Text(value, fontSize = 13.sp, color = valueColor, fontWeight = FontWeight.Medium)
    }
}
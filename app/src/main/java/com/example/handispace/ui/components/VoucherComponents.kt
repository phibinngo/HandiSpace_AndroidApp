package com.example.handispace.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.handispace.model.Voucher
import java.text.NumberFormat
import java.util.Locale

fun getSharedVoucherCategoryLabel(voucher: Voucher): String {
    if (voucher.type == "shipping") return "FREESHIP"
    if (voucher.applicable_categories.isEmpty()) return "TOÀN SÀN"
    return voucher.applicable_category_names.firstOrNull()?.uppercase() ?: "DANH MỤC"
}

fun getSharedDiscountText(voucher: Voucher): String {
    val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return if (voucher.discount_type == "percent") {
        if (voucher.max_discount > 0) "Giảm ${voucher.discount_value.toInt()}% (Tối đa ${formatVND.format(voucher.max_discount).replace("₫", "K")})"
        else "Giảm ${voucher.discount_value.toInt()}%"
    } else {
        "Giảm ${formatVND.format(voucher.discount_value).replace("₫", "K")}"
    }
}

@Composable
fun VoucherCardDisplay(
    voucher: Voucher,
    buttonText: String,
    isButtonEnabled: Boolean = true,
    isGrayOutButton: Boolean = false,
    onButtonClick: () -> Unit
) {
    val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val isFreeship = voucher.type == "shipping"
    val mainColor = if (isGrayOutButton && buttonText != "Đã lưu") Color.Gray else if (isFreeship) Color(0xFF0FA49C) else Color(0xFFEA580C)

    val labelText = getSharedVoucherCategoryLabel(voucher)
    val discountText = getSharedDiscountText(voucher)

    Row(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.White, RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(mainColor, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)), contentAlignment = Alignment.Center) {
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
                    color = if (isGrayOutButton && buttonText != "Đã lưu") Color.Gray else Color(0xFF1F2937),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Đơn tối thiểu ${formatVND.format(voucher.min_order_value)}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (buttonText == "Dùng Ngay" || buttonText == "Đã lưu") {
                OutlinedButton(
                    onClick = { if (buttonText != "Đã lưu") onButtonClick() },
                    border = BorderStroke(1.dp, mainColor),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.align(Alignment.BottomEnd).height(28.dp)
                ) { Text(buttonText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = mainColor) }
            } else {
                Button(
                    onClick = onButtonClick, enabled = isButtonEnabled,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isGrayOutButton) Color(0xFFE5E7EB) else mainColor, disabledContainerColor = Color(0xFFF3F4F6)),
                    shape = RoundedCornerShape(4.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp), modifier = Modifier.align(Alignment.BottomEnd).height(28.dp)
                ) { Text(text = buttonText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isGrayOutButton) Color.Gray else Color.White) }
            }
        }
    }
}

@Composable
fun VoucherCardSelect(
    voucher: Voucher,
    isSelected: Boolean,
    isEligible: Boolean,
    onSelect: () -> Unit
) {
    val isFreeship = voucher.type == "shipping"
    val mainColor = if (!isEligible) Color.LightGray else if (isFreeship) Color(0xFF0FA49C) else Color(0xFFEA580C)
    val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    val labelText = getSharedVoucherCategoryLabel(voucher)
    val discountText = getSharedDiscountText(voucher)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).height(80.dp)
            .border(1.dp, if (isSelected) Color(0xFFEA580C) else Color(0xFFE5E7EB), RoundedCornerShape(6.dp))
            .clickable(enabled = isEligible) { onSelect() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(mainColor, RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(labelText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }
        Row(modifier = Modifier.weight(3f).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(discountText, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isEligible) Color.Black else Color.Gray)
                Text("Đơn tối thiểu ${formatVND.format(voucher.min_order_value)}", fontSize = 11.sp, color = Color.Gray)
                if (!isEligible) Text("Chưa đủ giá trị đơn tối thiểu!", fontSize = 10.sp, color = Color.Red)
            }
            RadioButton(selected = isSelected, onClick = null, enabled = isEligible, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFEA580C)))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedVoucherBottomSheet(
    vouchers: List<Voucher>,
    selectedFreeshipId: String?,
    selectedDiscountId: String?,
    isEligible: (Voucher) -> Boolean,
    onApplyCode: (String, (Boolean, String) -> Unit) -> Unit,
    onAutoSelect: () -> Unit,
    onSelectFreeship: (Voucher?) -> Unit,
    onSelectDiscount: (Voucher?) -> Unit,
    onDismiss: () -> Unit
) {
    var codeInput by remember { mutableStateOf("") }
    var toastMessage by remember { mutableStateOf("") }

    val shippingList = vouchers.filter { it.type == "shipping" }.sortedByDescending { isEligible(it) }
    val orderList = vouchers.filter { it.type == "order" }.sortedByDescending { isEligible(it) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Color.White) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Text("Voucher của Shop", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = codeInput, onValueChange = { codeInput = it.uppercase() }, placeholder = { Text("Nhập mã giảm giá của bạn...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(6.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (codeInput.isNotBlank()) {
                            onApplyCode(codeInput) { success, msg ->
                                toastMessage = msg; if (success) codeInput = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)), shape = RoundedCornerShape(6.dp), modifier = Modifier.height(48.dp)
                ) { Text("Áp dụng", fontSize = 13.sp) }
            }
            if (toastMessage.isNotEmpty()) Text(toastMessage, fontSize = 12.sp, color = Color(0xFFEA580C), modifier = Modifier.padding(top = 4.dp))

            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = { onAutoSelect() }, modifier = Modifier.align(Alignment.End)) {
                Text("Tự động chọn mã tối ưu nhất ✨", color = Color(0xFFEA580C), fontWeight = FontWeight.Bold)
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                if (shippingList.isNotEmpty()) {
                    item { Text("Mã Miễn Phí Vận Chuyển", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp)) }
                    items(shippingList) { voucher ->
                        val eligible = isEligible(voucher)
                        VoucherCardSelect(
                            voucher = voucher, isSelected = selectedFreeshipId == voucher.voucher_id, isEligible = eligible,
                            onSelect = { if (eligible) onSelectFreeship(if (selectedFreeshipId == voucher.voucher_id) null else voucher) }
                        )
                    }
                }
                if (orderList.isNotEmpty()) {
                    item { Text("Mã Giảm Giá Đơn Hàng", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp)) }
                    items(orderList) { voucher ->
                        val eligible = isEligible(voucher)
                        VoucherCardSelect(
                            voucher = voucher, isSelected = selectedDiscountId == voucher.voucher_id, isEligible = eligible,
                            onSelect = { if (eligible) onSelectDiscount(if (selectedDiscountId == voucher.voucher_id) null else voucher) }
                        )
                    }
                }
            }
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)), modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(46.dp), shape = RoundedCornerShape(8.dp)) {
                Text("ĐỒNG Ý", fontWeight = FontWeight.Bold)
            }
        }
    }
}
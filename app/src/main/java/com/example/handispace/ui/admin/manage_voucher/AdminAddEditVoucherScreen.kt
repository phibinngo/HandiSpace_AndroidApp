package com.example.handispace.ui.admin.vouchers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.handispace.model.Category
import com.example.handispace.model.Voucher
import com.example.handispace.ui.components.AppTextField
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddEditVoucherScreen(
    navController: NavController,
    voucherId: String?,
    viewModel: AdminVoucherViewModel = hiltViewModel()
) {
    val isEditMode = voucherId != null
    val categories = viewModel.categories.value
    val actionState = viewModel.voucherActionState.value
    val orange600 = Color(0xFFEA580C)

    var code by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf("money") } // money hoặc percent
    var discountValue by remember { mutableStateOf("") }
    var minOrderValue by remember { mutableStateOf("") }
    var maxDiscount by remember { mutableStateOf("") }
    var usageLimit by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var originalVoucher by remember { mutableStateOf<Voucher?>(null) }

    LaunchedEffect(voucherId) {
        if (isEditMode) {
            val v = viewModel.allVouchers.value.find { it.voucher_id == voucherId }
            if (v != null) {
                originalVoucher = v
                code = v.code
                discountType = v.discount_type
                discountValue = v.discount_value.toLong().toString()
                minOrderValue = v.min_order_value.toLong().toString()
                maxDiscount = v.max_discount.toLong().toString()
                usageLimit = v.usage_limit.toString()
                if (v.applicable_categories.isNotEmpty()) {
                    selectedCategory = categories.find { it.category_id == v.applicable_categories.first() }
                }
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
                title = { Text(if (isEditMode) "Cập Nhật Voucher" else "Tạo Voucher Đặc Biệt Mới", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, tint = orange600, contentDescription = null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                Button(
                    onClick = {
                        val dVal = discountValue.toDoubleOrNull() ?: 0.0
                        val minOrder = minOrderValue.toDoubleOrNull() ?: 0.0
                        val maxD = maxDiscount.toDoubleOrNull() ?: 0.0
                        val limit = usageLimit.toIntOrNull() ?: 100

                        val builtVoucher = Voucher(
                            voucher_id = voucherId ?: "",
                            code = code.trim().uppercase(),
                            type = "order",
                            discount_type = discountType,
                            discount_value = dVal,
                            min_order_value = minOrder,
                            max_discount = if (discountType == "percent") maxD else 0.0,
                            usage_limit = limit,
                            used_count = originalVoucher?.used_count ?: 0,
                            applicable_categories = selectedCategory?.let { listOf(it.category_id) } ?: emptyList(),
                            applicable_category_names = selectedCategory?.let { listOf(it.name) } ?: emptyList(),
                            start_date = originalVoucher?.start_date ?: Timestamp.now(),
                            end_date = originalVoucher?.end_date ?: Timestamp(Timestamp.now().seconds + (60L * 24 * 3600), 0),
                            is_active = originalVoucher?.is_active ?: true
                        )

                        if (isEditMode) viewModel.updateVoucher(builtVoucher) else viewModel.addVoucher(builtVoucher)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = orange600),
                    enabled = code.isNotBlank() && discountValue.isNotBlank() && actionState?.isLoading != true
                ) { Text("XÁC NHẬN LƯU VOUCHER", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            AppTextField(value = code, onValueChange = { code = it }, label = "Mã giảm giá (Code)", placeholder = "VD: APPSIEUVIP", modifier = Modifier.fillMaxWidth())

            Column(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(12.dp)) {
                Text("Cách thức chiết khấu", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = discountType == "money", onClick = { discountType = "money" }, colors = RadioButtonDefaults.colors(selectedColor = orange600))
                    Text("Trừ tiền mặt trực tiếp (VND)", fontSize = 14.sp, modifier = Modifier.padding(end = 16.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = discountType == "percent", onClick = { discountType = "percent" }, colors = RadioButtonDefaults.colors(selectedColor = orange600))
                    Text("Chiết khấu theo phần trăm (%)", fontSize = 14.sp)
                }
            }

            AppTextField(value = discountValue, onValueChange = { discountValue = it }, label = if (discountType == "percent") "Mức giảm (%)" else "Mức giảm (VND)", placeholder = "0", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            AppTextField(value = minOrderValue, onValueChange = { minOrderValue = it }, label = "Giá trị đơn hàng tối thiểu áp dụng", placeholder = "0", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

            if (discountType == "percent") {
                AppTextField(value = maxDiscount, onValueChange = { maxDiscount = it }, label = "Số tiền giảm tối đa kịch trần", placeholder = "0", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            }

            AppTextField(value = usageLimit, onValueChange = { usageLimit = it }, label = "Tổng số lượt phát hành", placeholder = "100", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

            Column {
                Text("Ngành hàng áp dụng (Bỏ trống nếu áp dụng Toàn sàn)", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
                Box(modifier = Modifier.fillMaxWidth().height(52.dp).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)).background(Color.White).clickable { showCategoryMenu = true }.padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedCategory?.name ?: "Áp dụng Toàn Sàn (Mặc định)...", color = if (selectedCategory != null) Color.Black else Color.Gray)
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = showCategoryMenu, onDismissRequest = { showCategoryMenu = false }, modifier = Modifier.fillMaxWidth(0.9f)) {
                        DropdownMenuItem(text = { Text("Áp dụng Toàn Sàn") }, onClick = { selectedCategory = null; showCategoryMenu = false })
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.name) }, onClick = { selectedCategory = cat; showCategoryMenu = false })
                        }
                    }
                }
            }
        }
    }
}
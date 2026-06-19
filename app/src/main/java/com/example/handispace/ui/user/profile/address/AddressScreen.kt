package com.example.handispace.ui.user.profile.address

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handispace.model.Address
import com.example.handispace.ui.components.AppTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    navController: NavController,
    viewModel: AddressViewModel,
    isSelectionMode: Boolean = false
) {
    val userState = viewModel.userState.value
    val addresses = userState.data?.addresses ?: emptyList()

    var currentView by remember { mutableStateOf("LIST") }
    var addressToEdit by remember { mutableStateOf<Address?>(null) }

    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)
    val gray800 = Color(0xFF1F2937)

    if (currentView == "LIST") {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isSelectionMode) "Chọn địa chỉ" else "Địa chỉ của Tôi", fontSize = 18.sp, color = gray800) },
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
                    OutlinedButton(
                        onClick = {
                            addressToEdit = null
                            currentView = "FORM"
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = orange600),
                        border = BorderStroke(1.dp, orange600)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thêm Địa Chỉ Mới", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            containerColor = gray50
        ) { padding ->
            if (userState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = orange600) }
            } else if (addresses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Bạn chưa có địa chỉ nào.", color = Color.Gray) }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                    items(addresses) { address ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .clickable(enabled = isSelectionMode) {
                                    // Bắn ID qua trang trước
                                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_address_id", address.address_id)
                                    navController.popBackStack()
                                }
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(address.receiver_name.uppercase(), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = gray800)
                                Text(" | ${address.receiver_phone}", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(start = 8.dp).weight(1f))

                                if (!isSelectionMode) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Icon(Icons.Outlined.Edit, "Sửa", tint = orange600, modifier = Modifier.size(20.dp).clickable {
                                            addressToEdit = address
                                            currentView = "FORM"
                                        })
                                        Icon(Icons.Outlined.Delete, "Xóa", tint = orange600, modifier = Modifier.size(20.dp).clickable {
                                            viewModel.deleteAddress(address.address_id)
                                        })
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(address.street_address, fontSize = 14.sp, color = gray800)
                            Text("${address.ward}, ${address.province}", fontSize = 14.sp, color = gray800)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (address.is_default) {
                                    Box(modifier = Modifier.border(1.dp, orange600, RoundedCornerShape(2.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                        Text("Mặc định", color = orange600, fontSize = 11.sp)
                                    }
                                }
                                if (address.address_name.isNotEmpty()) {
                                    Box(modifier = Modifier.border(1.dp, Color.Gray, RoundedCornerShape(2.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                        Text(address.address_name, color = Color.Gray, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        Divider(color = gray50, thickness = 8.dp)
                    }
                }
            }
        }
    } else {
        var name by remember { mutableStateOf(addressToEdit?.receiver_name ?: "") }
        var phone by remember { mutableStateOf(addressToEdit?.receiver_phone ?: "") }
        var province by remember { mutableStateOf(addressToEdit?.province ?: "") }
        var ward by remember { mutableStateOf(addressToEdit?.ward ?: "") }
        var street by remember { mutableStateOf(addressToEdit?.street_address ?: "") }
        var type by remember { mutableStateOf(addressToEdit?.address_name ?: "Nhà Riêng") }
        var isDefault by remember { mutableStateOf(addressToEdit?.is_default ?: false) }
        var phoneError by remember { mutableStateOf("") }
        val context = LocalContext.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (addressToEdit == null) "Địa chỉ mới" else "Sửa địa chỉ", fontSize = 18.sp, color = gray800) },
                    navigationIcon = {
                        IconButton(onClick = { currentView = "LIST" }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Trở lại", tint = orange600)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                Box(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                    Button(
                        onClick = {
                            if (name.isBlank() || phone.isBlank() || province.isBlank() || ward.isBlank() || street.isBlank()) {
                                Toast.makeText(context, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val newAddr = Address(
                                address_id = addressToEdit?.address_id ?: "",
                                receiver_name = name,
                                receiver_phone = phone,
                                province = province,
                                ward = ward,
                                street_address = street,
                                address_name = type,
                                is_default = isDefault
                            )
                            viewModel.saveAddress(newAddr) {
                                currentView = "LIST"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = orange600),
                        enabled = phoneError.isEmpty() && phone.length == 10
                    ) {
                        Text("HOÀN THÀNH", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            },
            containerColor = gray50
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                    Text("Liên hệ", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                    AppTextField(value = name, onValueChange = { name = it }, label = "Họ và tên", placeholder = "Nhập họ và tên")
                    Spacer(modifier = Modifier.height(16.dp))
                    AppTextField(
                        value = phone,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                phone = input
                                phoneError = if (input.length != 10 && input.isNotEmpty()) "Số điện thoại phải có đúng 10 chữ số" else ""
                            }
                        },
                        label = "Số điện thoại",
                        placeholder = "Ví dụ: 0912345678",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    if (phoneError.isNotEmpty()) {
                        Text(text = phoneError, color = Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                    Text("Địa chỉ", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                    AppTextField(value = province, onValueChange = { province = it }, label = "Tỉnh/Thành phố", placeholder = "Nhập Tỉnh/Thành phố")
                    Spacer(modifier = Modifier.height(16.dp))
                    AppTextField(value = ward, onValueChange = { ward = it }, label = "Quận/Huyện, Phường/Xã", placeholder = "Nhập Quận/Huyện, Phường/Xã")
                    Spacer(modifier = Modifier.height(16.dp))
                    AppTextField(value = street, onValueChange = { street = it }, label = "Đường/Tòa nhà", placeholder = "Tên đường, Tòa nhà, Số nhà")
                }
                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Loại địa chỉ", fontSize = 15.sp, color = gray800)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChoiceChip(text = "Văn Phòng", isSelected = type == "Văn Phòng") { type = "Văn Phòng" }
                            ChoiceChip(text = "Nhà Riêng", isSelected = type == "Nhà Riêng") { type = "Nhà Riêng" }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Đặt làm địa chỉ mặc định", fontSize = 15.sp, color = gray800)
                        Switch(
                            checked = isDefault,
                            onCheckedChange = { isDefault = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = orange600)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChoiceChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) Color(0xFFFFF7ED) else Color(0xFFF3F4F6))
            .border(1.dp, if (isSelected) Color(0xFFEA580C) else Color.Transparent, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) Color(0xFFEA580C) else Color(0xFF1F2937), fontSize = 13.sp)
    }
}
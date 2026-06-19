package com.example.handispace.ui.user.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.NumberFormat
import java.util.Locale

// Data class để giữ kiểu dữ liệu an toàn 100%
data class RankProgress(val currentRank: String, val nextRank: String, val nextTarget: Double, val progress: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoyaltyScreen(navController: NavController, viewModel: ProfileViewModel) {
    val state = viewModel.userState.value
    val user = state.data ?: return

    val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val totalSpent = user.total_spent

    val orange600 = Color(0xFFEA580C)
    val gray50 = Color(0xFFF9FAFB)
    val gray800 = Color(0xFF1F2937)

    // LOGIC TÍNH TOÁN HẠNG CHUẨN KOTLIN
    val targetGold = 5_000_000.0
    val targetSilver = 2_000_000.0
    val targetDiamond = 10_000_000.0

    val rankInfo = when {
        totalSpent >= targetDiamond -> RankProgress("Kim Cương", "Tối đa", targetDiamond, 1f)
        totalSpent >= targetGold -> RankProgress("Vàng", "Kim Cương", targetDiamond, (totalSpent / targetDiamond).toFloat())
        totalSpent >= targetSilver -> RankProgress("Bạc", "Vàng", targetGold, (totalSpent / targetGold).toFloat())
        else -> RankProgress("Thành viên", "Bạc", targetSilver, (totalSpent / targetSilver).toFloat())
    }

    val amountLeft = (rankInfo.nextTarget - totalSpent).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đặc quyền Thành viên", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = gray800) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Trở lại", tint = orange600)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = gray50
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

            // KHỐI 1: HIỂN THỊ RANK HIỆN TẠI VÀ PROGRESS BAR
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Hạng của bạn hiện tại", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(rankInfo.currentRank.uppercase(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = orange600)

                    Spacer(modifier = Modifier.height(24.dp))

                    if (rankInfo.currentRank != "Kim Cương") {
                        LinearProgressIndicator(
                            progress = rankInfo.progress,
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                            color = orange600,
                            trackColor = Color(0xFFF3F4F6)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Mua thêm ${formatVND.format(amountLeft)} để thăng hạng ${rankInfo.nextRank}",
                            fontSize = 14.sp, fontWeight = FontWeight.Medium, color = gray800, textAlign = TextAlign.Center
                        )
                    } else {
                        Text(text = "Chúc mừng! Bạn đã đạt hạng cao nhất.", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = orange600)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Tổng chi tiêu: ${formatVND.format(totalSpent)}", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // KHỐI 2: BẢNG GIÁ TRỊ VÀ QUYỀN LỢI
            Text("Chi tiết đặc quyền", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = gray800)
            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.White).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))) {
                RankInfoRow(rank = "Thành viên", condition = "Đăng ký mới", benefit = "Không giảm giá")
                Divider(color = gray50)
                RankInfoRow(rank = "Bạc 🥈", condition = "Từ 2 Triệu VNĐ", benefit = "Giảm 2% mọi đơn")
                Divider(color = gray50)
                RankInfoRow(rank = "Vàng 🥇", condition = "Từ 5 Triệu VNĐ", benefit = "Giảm 5% mọi đơn")
                Divider(color = gray50)
                RankInfoRow(rank = "Kim Cương 💎", condition = "Từ 10 Triệu VNĐ", benefit = "Giảm 8% mọi đơn")
            }
        }
    }
}

@Composable
fun RankInfoRow(rank: String, condition: String, benefit: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(rank, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Text(condition, fontSize = 12.sp, color = Color.Gray)
        }
        Text(benefit, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFFEA580C))
    }
}
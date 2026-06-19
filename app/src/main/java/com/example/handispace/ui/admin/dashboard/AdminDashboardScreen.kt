package com.example.handispace.ui.admin.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    val gray50 = Color(0xFFF9FAFB)
    val gray800 = Color(0xFF1F2937)
    val orange600 = Color(0xFFEA580C)

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = orange600)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(gray50).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. DOANH THU HÔM NAY (Thẻ To đập vào mắt)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(140.dp),
                colors = CardDefaults.cardColors(containerColor = orange600),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DOANH THU HÔM NAY", color = Color(0xFFFFEDD5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(formatVND.format(state.todaysRevenue), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // 2. LƯỚI THỐNG KÊ TỔNG QUAN
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniStatCard(Modifier.weight(1f), "Tổng doanh thu", formatVND.format(state.totalRevenue), Icons.Filled.Payments, Color(0xFF10B981))
                    MiniStatCard(Modifier.weight(1f), "Tổng đơn hàng", "${state.totalOrders} đơn", Icons.Filled.Assignment, Color(0xFF3B82F6))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniStatCard(Modifier.weight(1f), "Sản phẩm", "${state.totalProducts} SP", Icons.Filled.Inventory, Color(0xFFF59E0B))
                    MiniStatCard(Modifier.weight(1f), "Khách hàng", "${state.totalUsers} User", Icons.Filled.People, Color(0xFF8B5CF6))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniStatCard(Modifier.weight(1f), "Danh mục", "${state.totalCategories} Mục", Icons.Filled.Category, Color(0xFFEC4899))
                    Spacer(modifier = Modifier.weight(1f)) // Chỗ trống cho cân xứng
                }
            }
        }

        // 3. BIỂU ĐỒ DOANH THU 6 THÁNG
        item {
            Card(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Biểu đồ doanh thu (6 tháng gần nhất)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = gray800)
                    Spacer(modifier = Modifier.height(24.dp))

                    // GỌI COMPONENT CANVAS VẼ BIỂU ĐỒ NẰM DƯỚI CÙNG FILE NÀY
                    RevenueLineChart(data = state.revenueChartData, modifier = Modifier.fillMaxWidth().height(180.dp))
                }
            }
        }

        // 4. TOP SẢN PHẨM BÁN CHẠY
        item {
            Text("Top Sản phẩm bán chạy", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = gray800, modifier = Modifier.padding(top = 8.dp))
        }

        itemsIndexed(state.topProducts) { index, product ->
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Huy hiệu Hạng 1, 2, 3
                val badgeColor = when(index) {
                    0 -> Color(0xFFF59E0B) // Vàng
                    1 -> Color(0xFF9CA3AF) // Bạc
                    2 -> Color(0xFFD97706) // Đồng
                    else -> Color.LightGray
                }
                Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(badgeColor), contentAlignment = Alignment.Center) {
                    Text("#${index + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = gray800, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(formatVND.format(product.price), fontSize = 13.sp, color = orange600)
                }

                Text("Đã bán: ${product.sold_count}", fontSize = 12.sp, color = Color.Gray)
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

// ==========================================
// THẺ THỐNG KÊ MINI
// ==========================================
@Composable
fun MiniStatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, iconColor: Color) {
    Card(
        modifier = modifier.height(90.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ==========================================
// BIỂU ĐỒ ĐƯỜNG TỰ CHẾ (CANVAS CỰC NHẸ & MƯỢT)
// ==========================================
@Composable
fun RevenueLineChart(data: List<MonthlyRevenue>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return

    val maxRevenue = data.maxOf { it.revenue }.toFloat()
    val minRevenue = 0f
    val orange600 = Color(0xFFEA580C)

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 24.dp, top = 8.dp)) {
            val width = size.width
            val height = size.height
            val stepX = width / (data.size - 1).coerceAtLeast(1)

            // Vẽ các đường lưới ngang mờ mờ cho đẹp
            for (i in 0..4) {
                val y = height - (i * height / 4)
                drawLine(color = Color(0xFFF3F4F6), start = Offset(0f, y), end = Offset(width, y), strokeWidth = 2f)
            }

            val path = Path()
            var lastX = 0f
            var lastY = 0f

            data.forEachIndexed { index, item ->
                val x = index * stepX
                val y = height - ((item.revenue.toFloat() - minRevenue) / (maxRevenue - minRevenue) * height)

                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)

                // Vẽ các điểm chấm tròn
                drawCircle(color = orange600, radius = 6.dp.toPx(), center = Offset(x, y))
                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(x, y))

                lastX = x
                lastY = y
            }

            // Vẽ đường Line biểu đồ
            drawPath(
                path = path,
                color = orange600,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Tên các tháng (T1, T2...) nằm ở dưới đáy
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { item ->
                Text(text = item.month, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}
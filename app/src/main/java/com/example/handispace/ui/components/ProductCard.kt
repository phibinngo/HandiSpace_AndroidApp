package com.example.handispace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.handispace.model.Product
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val isOutOfStock = product.quantity <= 0

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                val imageUrl = if (product.images.isNotEmpty()) product.images[0] else ""
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().background(Color.White)
                )

                if (isOutOfStock) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("HẾT HÀNG", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = product.name, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(34.dp),
                    color = if (isOutOfStock) Color.Gray else Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Giá
                Text(
                    text = formatVND.format(product.price),
                    color = if (isOutOfStock) Color.Gray else Color(0xFFEA580C),
                    fontSize = 14.sp, fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 🔥 ĐÃ THÊM SAO ĐÁNH GIÁ & FIX LƯỢT BÁN CHUẨN
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFF59E0B), modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = String.format("%.1f", product.rating_average), fontSize = 10.sp, color = Color.Gray)
                    }
                    Text(text = "Đã bán ${product.sold_count}", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}
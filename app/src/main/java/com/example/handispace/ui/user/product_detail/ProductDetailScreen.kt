package com.example.handispace.ui.user.product_detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.handispace.navigation.Routes
import com.example.handispace.ui.components.ErrorPopup
import com.example.handispace.ui.components.ProductCard
import com.example.handispace.ui.components.SuccessPopup
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String,
    viewModel: ProductDetailViewModel
) {
    LaunchedEffect(productId) { viewModel.getProductDetail(productId) }

    val state = viewModel.productState.value
    val product = state.data
    val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val context = LocalContext.current

    val suggestedState = viewModel.suggestedProductsState.value
    val safeSuggested = suggestedState.data ?: emptyList()

    val isOutOfStock = product?.quantity == 0
    var quantity by remember { mutableStateOf(1) }
    LaunchedEffect(isOutOfStock) { if (isOutOfStock) quantity = 0 }

    val addToCartState = viewModel.addToCartState.value
    var showSuccessPopup by remember { mutableStateOf(false) }
    var showErrorPopup by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(addToCartState.data, addToCartState.errorMessage) {
        if (addToCartState.data != null) {
            showSuccessPopup = true
            viewModel.resetAddToCartState()
        } else if (addToCartState.errorMessage.isNotEmpty()) {
            errorMessage = addToCartState.errorMessage
            showErrorPopup = true
            viewModel.resetAddToCartState()
        }
    }

    LaunchedEffect(showSuccessPopup) { if (showSuccessPopup) { delay(2000); showSuccessPopup = false } }
    LaunchedEffect(showErrorPopup) { if (showErrorPopup) { delay(1500); showErrorPopup = false } }

    val orange600 = Color(0xFFEA580C)
    val orange500 = Color(0xFFF97316)
    val orange50 = Color(0xFFFFF7ED)
    val gray50 = Color(0xFFF9FAFB)
    val gray300 = Color(0xFFD1D5DB)
    val gray500 = Color(0xFF6B7280)
    val gray800 = Color(0xFF1F2937)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sản phẩm", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = gray800, fontFamily = FontFamily.SansSerif) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, contentDescription = "Trở lại", tint = orange600) }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.CART) }) { Icon(Icons.Outlined.ShoppingCart, contentDescription = "Giỏ hàng", tint = orange600) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (product != null) {
                Row(modifier = Modifier.fillMaxWidth().height(56.dp).background(Color.White)) {
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight().background(orange50).clickable {
                            val adminId = "axTeqn5PxRet3M5vYr1nvgbnp542"
                            val adminName = android.net.Uri.encode("CSKH HandiSpace")
                            val msg = android.net.Uri.encode("Xin chào, tôi muốn hỏi thêm thông tin về sản phẩm: ${product.name}")

                            navController.navigate("chat/$adminId/$adminName?context=$msg")
                        },
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.ChatBubbleOutline, contentDescription = "Chat", tint = orange500, modifier = Modifier.size(20.dp))
                        Text("Chat ngay", fontSize = 10.sp, color = orange500, fontWeight = FontWeight.Medium)
                    }

                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(gray300))

                    Column(
                        modifier = Modifier.weight(1.2f).fillMaxHeight().background(if (isOutOfStock) gray300 else orange50).clickable(enabled = !isOutOfStock && !addToCartState.isLoading) {
                            viewModel.addToCart(product, quantity)
                        },
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                    ) {
                        if (addToCartState.isLoading) {
                            CircularProgressIndicator(color = orange500, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.ShoppingCart, contentDescription = "Thêm giỏ", tint = if (isOutOfStock) Color.White else orange500, modifier = Modifier.size(20.dp))
                            Text(if (isOutOfStock) "HẾT HÀNG" else "Thêm vào Giỏ", fontSize = 10.sp, color = if (isOutOfStock) Color.White else orange500, fontWeight = FontWeight.Medium)
                        }
                    }

                    Column(
                        modifier = Modifier.weight(2f).fillMaxHeight().background(if (isOutOfStock) gray500 else Color(0xFFC2410C))
                            .clickable(enabled = !isOutOfStock) {
                                navController.navigate("${Routes.CHECKOUT}/BUYNOW_${product.product_id}_${quantity}/none/none")
                            },
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                    ) {
                        Text(if (isOutOfStock) "HẾT HÀNG" else "Mua ngay", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        if (!isOutOfStock) Text(formatVND.format(product.price), fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        },
        containerColor = gray50
    ) { padding ->

        if (showErrorPopup) { ErrorPopup(errorMessage = errorMessage, onDismiss = { showErrorPopup = false }) }
        if (showSuccessPopup) { SuccessPopup(message = "Đã thêm vào giỏ hàng", onDismiss = { showSuccessPopup = false }) }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = orange600) }
        } else if (state.errorMessage.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.errorMessage, color = Color.Red) }
        } else if (product != null) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {

                // ==========================================
                // 🔥 NÂNG CẤP: SLIDER VUỐT TAY MANUAL
                // ==========================================
                val imageList = product.images

                if (imageList.isNotEmpty()) {
                    val pagerState = rememberPagerState(pageCount = { imageList.size })

                    // 🔥 Đã xóa LaunchedEffect auto-scroll ở đây!

                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color.White)) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            AsyncImage(
                                model = imageList[page],
                                contentDescription = "Ảnh sản phẩm",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Dải chấm tròn nhỏ nhỏ báo hiệu đang ở hình số mấy
                        if (imageList.size > 1) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                repeat(imageList.size) { iteration ->
                                    val color = if (pagerState.currentPage == iteration) orange600 else Color.LightGray.copy(alpha = 0.8f)
                                    val size = if (pagerState.currentPage == iteration) 8.dp else 6.dp
                                    Box(
                                        modifier = Modifier
                                            .size(size)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Nếu không có ảnh nào (lỗi data), thì hiện 1 ô trắng
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color.LightGray), contentAlignment = Alignment.Center) {
                        Text("Sản phẩm không có ảnh", color = Color.Gray)
                    }
                }
                // ==========================================
                // HẾT KHU VỰC ẢNH
                // ==========================================

                Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(product.name, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = gray800, fontFamily = FontFamily.SansSerif, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 26.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(formatVND.format(product.price), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = orange600)

                            if (isOutOfStock) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.background(gray300, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("HẾT HÀNG", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text("Đã bán: ${product.sold_count}", fontSize = 13.sp, color = gray500)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Phí vận chuyển:", fontSize = 14.sp, color = gray500, modifier = Modifier.width(120.dp))
                    Text("30.000đ (Miễn phí từ 100k)", fontSize = 14.sp, color = gray800)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Số lượng", color = gray500, fontSize = 14.sp, modifier = Modifier.width(120.dp))

                    Row(modifier = Modifier.border(1.dp, gray300, RoundedCornerShape(4.dp)).height(32.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.width(32.dp).fillMaxHeight().clickable(enabled = !isOutOfStock) {
                                if (quantity > 1) quantity--
                            },
                            contentAlignment = Alignment.Center
                        ) { Text("-", color = if (isOutOfStock) gray300 else gray800, fontSize = 16.sp) }

                        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(gray300))

                        Box(modifier = Modifier.width(48.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            Text(if (isOutOfStock) "0" else quantity.toString(), color = if (isOutOfStock) gray300 else gray800, fontSize = 14.sp)
                        }

                        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(gray300))

                        Box(
                            modifier = Modifier.width(32.dp).fillMaxHeight().clickable(enabled = !isOutOfStock) {
                                if (quantity < product.quantity) {
                                    quantity++
                                } else {
                                    errorMessage = "Số lượng hàng còn lại là ${product.quantity}"
                                    showErrorPopup = true
                                }
                            },
                            contentAlignment = Alignment.Center
                        ) { Text("+", color = if (isOutOfStock) gray300 else gray800, fontSize = 16.sp) }
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Kho: ${product.quantity}", color = if (isOutOfStock) Color.Red else gray300, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                    Text("MÔ TẢ SẢN PHẨM", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = gray800)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(product.description, fontSize = 14.sp, color = gray500, lineHeight = 22.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = gray50, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Kho: ${product.quantity} sản phẩm có sẵn", fontSize = 13.sp, color = gray500)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // KHU VỰC HIỂN THỊ ĐÁNH GIÁ SẢN PHẨM
                val reviewList = viewModel.reviewsState.value.data ?: emptyList()
                var selectedFilter by remember { mutableStateOf(0) }

                Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                    Text("ĐÁNH GIÁ SẢN PHẨM", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = gray800)

                    if (reviewList.isEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Sản phẩm chưa có đánh giá nào.", color = gray500, fontSize = 14.sp)
                    } else {
                        val avgRating = reviewList.map { it.rating }.average()
                        Row(modifier = Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(String.format("%.1f", avgRating), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = orange600)
                            Text(" / 5", fontSize = 14.sp, color = gray500, modifier = Modifier.align(Alignment.Bottom))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("(${reviewList.size} đánh giá)", fontSize = 13.sp, color = gray500, modifier = Modifier.align(Alignment.Bottom))
                        }

                        val filters = listOf(0 to "Tất cả", 5 to "5 Sao", 4 to "4 Sao", 3 to "3 Sao", 2 to "2 Sao", 1 to "1 Sao")
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            filters.forEach { (stars, label) ->
                                val count = if (stars == 0) reviewList.size else reviewList.count { it.rating.toInt() == stars }
                                val isSelected = selectedFilter == stars
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) orange50 else gray50)
                                        .border(1.dp, if (isSelected) orange600 else gray300, RoundedCornerShape(16.dp))
                                        .clickable { selectedFilter = stars }.padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text("$label ($count)", color = if (isSelected) orange600 else gray800, fontSize = 13.sp)
                                }
                            }
                        }

                        val displayReviews = if (selectedFilter == 0) reviewList else reviewList.filter { it.rating.toInt() == selectedFilter }

                        displayReviews.forEach { rev ->
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(16.dp)).background(gray300), contentAlignment = Alignment.Center) {
                                        Text(rev.user_name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(rev.user_name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = gray800)
                                        Row {
                                            for (i in 1..5) {
                                                Icon(
                                                    imageVector = Icons.Filled.Star,
                                                    contentDescription = null,
                                                    tint = if (i <= rev.rating) Color(0xFFF59E0B) else gray300,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(rev.content.ifBlank { "Người dùng không để lại nhận xét." }, fontSize = 14.sp, color = gray800, lineHeight = 20.sp)
                            }
                            Divider(color = gray50, thickness = 1.dp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                    Text("SẢN PHẨM TƯƠNG TỰ", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = gray800)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (suggestedState.isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = orange600)
                        }
                    } else if (safeSuggested.isEmpty()) {
                        Text("Chưa có sản phẩm tương tự nào cùng danh mục.", color = gray500, fontSize = 14.sp)
                    } else {
                        val chunkedProducts = safeSuggested.chunked(2)

                        chunkedProducts.forEach { rowProducts ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ProductCard(product = rowProducts[0], onClick = {
                                        navController.navigate(Routes.PRODUCT_DETAIL.replace("{productId}", rowProducts[0].product_id))
                                    })
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    if (rowProducts.size > 1) {
                                        ProductCard(product = rowProducts[1], onClick = {
                                            navController.navigate(Routes.PRODUCT_DETAIL.replace("{productId}", rowProducts[1].product_id))
                                        })
                                    } else {
                                        Spacer(modifier = Modifier.fillMaxWidth())
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
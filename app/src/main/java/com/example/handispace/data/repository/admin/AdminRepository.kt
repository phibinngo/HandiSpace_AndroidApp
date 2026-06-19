package com.example.handispace.data.repository.admin

import com.example.handispace.model.Product
import com.example.handispace.ui.admin.dashboard.MonthlyRevenue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class AdminRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    // ==========================================
    // 🔥 1. HÀM MỚI: LẤY TOP SẢN PHẨM REAL-TIME (Dựa vào sold_count)
    // ==========================================
    fun getTopProductsRealtime(limit: Long = 5): Flow<List<Product>> = callbackFlow {
        val listener = db.collection("products")
            .orderBy("sold_count", Query.Direction.DESCENDING) // Lấy chuẩn lượt bán thật
            .limit(limit)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                val topProducts = snap?.documents?.mapNotNull { it.toObject(Product::class.java) } ?: emptyList()
                trySend(topProducts)
            }
        awaitClose { listener.remove() }
    }

    // ==========================================
    // 2. HÀM LẤY THỐNG KÊ TỔNG QUAN (Đã rút Top Products ra ngoài)
    // ==========================================
    suspend fun getDashboardStats(): DashboardData? = coroutineScope {
        try {
            val productsDef = async { db.collection("products").get().await() }
            val categoriesDef = async { db.collection("categories").get().await() }
            val usersDef = async { db.collection("users").whereEqualTo("role", "customer").get().await() }
            val ordersDef = async { db.collection("orders").get().await() }

            val productsSnap = productsDef.await()
            val categoriesSnap = categoriesDef.await()
            val usersSnap = usersDef.await()
            val ordersSnap = ordersDef.await()

            var totalRevenue = 0.0
            var todaysRevenue = 0.0
            val monthlyMap = mutableMapOf<String, Double>()

            val sdfMonth = SimpleDateFormat("MM/yyyy", Locale.getDefault())
            val sdfDay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val todayStr = sdfDay.format(Date())

            ordersSnap.documents.forEach { doc ->
                val status = doc.getString("status") ?: ""

                if (status == "completed") {
                    val finalTotal = doc.getDouble("final_total") ?: 0.0
                    totalRevenue += finalTotal

                    val timestamp = doc.getTimestamp("created_at")
                    if (timestamp != null) {
                        val date = timestamp.toDate()

                        if (sdfDay.format(date) == todayStr) {
                            todaysRevenue += finalTotal
                        }

                        val monthStr = sdfMonth.format(date)
                        monthlyMap[monthStr] = (monthlyMap[monthStr] ?: 0.0) + finalTotal
                    }
                }
            }

            val chartData = monthlyMap.map { MonthlyRevenue(it.key, it.value) }.sortedBy { it.month }

            DashboardData(
                todaysRevenue = todaysRevenue,
                totalRevenue = totalRevenue,
                totalOrders = ordersSnap.size(),
                totalProducts = productsSnap.size(),
                totalCategories = categoriesSnap.size(),
                totalUsers = usersSnap.size(),
                revenueChartData = chartData
            )
        } catch (e: Exception) {
            null
        }
    }
}

// Data class này cũng đã được dọn sạch biến topProducts cũ
data class DashboardData(
    val todaysRevenue: Double,
    val totalRevenue: Double,
    val totalOrders: Int,
    val totalProducts: Int,
    val totalCategories: Int,
    val totalUsers: Int,
    val revenueChartData: List<MonthlyRevenue>
)
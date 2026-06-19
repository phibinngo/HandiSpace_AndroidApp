package com.example.handispace.ui.admin.vouchers

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.admin.AdminCategoryRepository
import com.example.handispace.data.repository.admin.AdminVoucherRepository
import com.example.handispace.model.Category
import com.example.handispace.model.Voucher
import com.example.handispace.utils.ResultState
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AdminVoucherViewModel @Inject constructor(
    private val adminVoucherRepo: AdminVoucherRepository,
    private val categoryRepo: AdminCategoryRepository
) : ViewModel() {

    val allVouchers = mutableStateOf<List<Voucher>>(emptyList())
    val categories = mutableStateOf<List<Category>>(emptyList())
    val selectedTab = mutableStateOf("Freeship")
    val isLoading = mutableStateOf(true)

    val voucherActionState = mutableStateOf<ResultState<Boolean>?>(null)

    val tabs = listOf("Freeship", "Toàn sàn", "Theo danh mục")

    init {
        loadVoucherData()
    }

    private fun loadVoucherData() {
        viewModelScope.launch {
            launch {
                categoryRepo.getCategoriesRealtime().collectLatest { cats ->
                    categories.value = cats
                }
            }
            launch {
                adminVoucherRepo.getAllVouchersAdminRealtime().collectLatest { vouchers ->
                    allVouchers.value = vouchers
                    checkAndGenerateMonthlyVouchers(vouchers)
                    isLoading.value = false
                }
            }
        }
    }

    // 🔥 CỖ MÁY SẢN XUẤT CƠN MƯA VOUCHER
    // 🔥 CỖ MÁY SẢN XUẤT CƠN MƯA VOUCHER VÀ BẢO VỆ FREESHIP
    private suspend fun checkAndGenerateMonthlyVouchers(currentVouchers: List<Voucher>) {

        // 0. BẢO VỆ MÃ FREESHIP TRẤN PHÁI (Lỡ tay xóa sạch DB thì nó tự mọc lại ngay)
        if (currentVouchers.none { it.type == "shipping" && it.code == "FREESHIP_AUTO" }) {
            adminVoucherRepo.addVoucher(Voucher(
                code = "FREESHIP_AUTO",
                type = "shipping",
                discount_type = "money",
                discount_value = 30000.0,
                min_order_value = 100000.0,
                max_discount = 0.0,
                usage_limit = 0, // 0 = Vô hạn
                target_audience = "all",
                is_active = true,
                start_date = Timestamp.now(),
                end_date = Timestamp(Timestamp.now().seconds + (3650L * 24 * 3600), 0) // Hạn dùng 10 năm
            ))
        }

        // 1. XỬ LÝ ĐẺ MÃ THÁNG
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        val prefix = "HD${month}${year}" // VD: HD62026

        if (currentVouchers.none { it.code.startsWith(prefix) }) {

            // Dừng 1 giây để hứng đủ danh sách Category từ DB về
            kotlinx.coroutines.delay(1000)

            val expireTime = Timestamp(Timestamp.now().seconds + (30L * 24 * 3600), 0)

            // Kho thông số Random lẹt đẹt thực tế
            val percentList = listOf(10.0, 12.0, 15.0, 20.0)
            val maxDiscountList = listOf(15000.0, 20000.0, 30000.0, 50000.0)
            val moneyList = listOf(15000.0, 25000.0, 30000.0, 50000.0)
            val minOrderList = listOf(50000.0, 99000.0, 150000.0, 250000.0, 300000.0)

            // 1. TẠO 10 VOUCHER TOÀN SÀN
            for (i in 1..10) {
                val isPercent = listOf(true, false).random()
                val minOrder = minOrderList.random()

                if (isPercent) {
                    val percent = percentList.random()
                    val maxD = maxDiscountList.random()
                    adminVoucherRepo.addVoucher(Voucher(
                        code = "${prefix}ALLP$i", type = "order", discount_type = "percent", discount_value = percent,
                        min_order_value = minOrder, max_discount = maxD, usage_limit = 1000, target_audience = "all", is_active = true,
                        start_date = Timestamp.now(), end_date = expireTime
                    ))
                } else {
                    val money = moneyList.random()
                    val safeMinOrder = if (minOrder <= money) money + 50000.0 else minOrder
                    adminVoucherRepo.addVoucher(Voucher(
                        code = "${prefix}ALLM$i", type = "order", discount_type = "money", discount_value = money,
                        min_order_value = safeMinOrder, max_discount = 0.0, usage_limit = 1000, target_audience = "all", is_active = true,
                        start_date = Timestamp.now(), end_date = expireTime
                    ))
                }
            }

            // 2. TẠO 5-7 VOUCHER CHO TỪNG DANH MỤC
            val cats = categories.value
            cats.forEachIndexed { index, cat ->
                val numOfVouchers = (5..7).random()
                for (j in 1..numOfVouchers) {
                    val isPercent = listOf(true, false).random()
                    val minOrder = minOrderList.random()

                    if (isPercent) {
                        val percent = percentList.random()
                        val maxD = maxDiscountList.random()
                        adminVoucherRepo.addVoucher(Voucher(
                            code = "${prefix}C${index}P$j", type = "order", discount_type = "percent", discount_value = percent,
                            min_order_value = minOrder, max_discount = maxD, usage_limit = 1000, target_audience = "all",
                            applicable_categories = listOf(cat.category_id), applicable_category_names = listOf(cat.name),
                            is_active = true, start_date = Timestamp.now(), end_date = expireTime
                        ))
                    } else {
                        val money = moneyList.random()
                        val safeMinOrder = if (minOrder <= money) money + 40000.0 else minOrder
                        adminVoucherRepo.addVoucher(Voucher(
                            code = "${prefix}C${index}M$j", type = "order", discount_type = "money", discount_value = money,
                            min_order_value = safeMinOrder, max_discount = 0.0, usage_limit = 1000, target_audience = "all",
                            applicable_categories = listOf(cat.category_id), applicable_category_names = listOf(cat.name),
                            is_active = true, start_date = Timestamp.now(), end_date = expireTime
                        ))
                    }
                }
            }
        }
    }
    fun getFilteredVouchers(): List<Voucher> {
        val list = allVouchers.value
        return when (selectedTab.value) {
            "Freeship" -> list.filter { it.type == "shipping" }
            "Toàn sàn" -> list.filter { it.type == "order" && it.applicable_categories.isEmpty() }
            "Theo danh mục" -> list.filter { it.type == "order" && it.applicable_categories.isNotEmpty() }
            else -> list
        }
    }

    fun addVoucher(voucher: Voucher) {
        viewModelScope.launch {
            voucherActionState.value = ResultState(isLoading = true)
            voucherActionState.value = adminVoucherRepo.addVoucher(voucher)
        }
    }

    fun updateVoucher(voucher: Voucher) {
        viewModelScope.launch {
            voucherActionState.value = ResultState(isLoading = true)
            voucherActionState.value = adminVoucherRepo.updateVoucher(voucher)
        }
    }

    fun deleteVoucher(voucherId: String) {
        viewModelScope.launch {
            adminVoucherRepo.deleteVoucher(voucherId)
        }
    }

    fun clearActionState() { voucherActionState.value = null }
}
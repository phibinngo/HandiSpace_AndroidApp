package com.example.handispace.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

import com.example.handispace.ui.auth.AuthViewModel
import com.example.handispace.ui.auth.LoginScreen
import com.example.handispace.ui.auth.RegisterScreen
import com.example.handispace.ui.user.UserLayout
import com.example.handispace.ui.user.cart.CartScreen
import com.example.handispace.ui.user.cart.CartViewModel
import com.example.handispace.ui.user.checkout.CheckoutScreen
import com.example.handispace.ui.user.checkout.CheckoutViewModel
import com.example.handispace.ui.user.order.detail.OrderDetailScreen
import com.example.handispace.ui.user.order.detail.OrderDetailViewModel
import com.example.handispace.ui.user.product_detail.ProductDetailScreen
import com.example.handispace.ui.user.product_detail.ProductDetailViewModel
import com.example.handispace.ui.user.profile.EditProfileScreen
import com.example.handispace.ui.user.profile.ProfileViewModel
import com.example.handispace.ui.user.profile.address.AddressViewModel
import com.example.handispace.ui.user.profile.address.AddressScreen
import com.example.handispace.ui.user.search.SearchScreen
import com.example.handispace.ui.user.search.SearchViewModel
import com.example.handispace.ui.user.profile.myvoucher.MyVoucherScreen
import com.example.handispace.ui.user.profile.myvoucher.MyVoucherViewModel
import com.example.handispace.ui.chat.ChatListScreen
import com.example.handispace.ui.user.aichat.AiChatScreen

@Composable
fun AppNavigation() {
    val mainNavController = rememberNavController()

    NavHost(
        navController = mainNavController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            LaunchedEffect(Unit) {
                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser

                if (user == null) {
                    mainNavController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                } else {
                    try {
                        val db = FirebaseFirestore.getInstance()
                        val doc = db.collection("users").document(user.uid).get().await()
                        val role = doc.getString("role") ?: "customer"

                        if (role == "admin") {
                            mainNavController.navigate(Routes.ADMIN_DASHBOARD) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        } else {
                            mainNavController.navigate(Routes.HOME) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    } catch (e: Exception) {
                        mainNavController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFEA580C))
            }
        }

        authGraph(mainNavController)
        userGraph(mainNavController)
        adminGraph(mainNavController)
    }
}

fun NavGraphBuilder.authGraph(navController: NavController) {
    composable(Routes.LOGIN) {
        val authViewModel: AuthViewModel = hiltViewModel()
        LoginScreen(navController = navController, viewModel = authViewModel)
    }

    composable(Routes.REGISTER) {
        val authViewModel: AuthViewModel = hiltViewModel()
        RegisterScreen(navController = navController, viewModel = authViewModel)
    }

    composable(Routes.FORGOT_PASSWORD) {
        val forgotPasswordViewModel: com.example.handispace.ui.auth.forgot_password.ForgotPasswordViewModel = hiltViewModel()
        com.example.handispace.ui.auth.forgot_password.ForgotPasswordScreen(
            navController = navController,
            viewModel = forgotPasswordViewModel
        )
    }
}


fun NavGraphBuilder.userGraph(navController: NavController) {
    composable(Routes.HOME) {
        UserLayout(mainNavController = navController)
    }

    composable(Routes.SEARCH) {
        val searchViewModel: SearchViewModel = hiltViewModel()
        SearchScreen(navController = navController, viewModel = searchViewModel)
    }

    composable(
        route = Routes.PRODUCT_DETAIL,
        arguments = listOf(navArgument("productId") { type = NavType.StringType })
    ) { backStackEntry ->
        val productId = backStackEntry.arguments?.getString("productId") ?: ""
        val viewModel: ProductDetailViewModel = hiltViewModel()
        ProductDetailScreen(navController = navController, productId = productId, viewModel = viewModel)
    }

    composable(Routes.CART) {
        val cartViewModel: CartViewModel = hiltViewModel()
        CartScreen(navController = navController, viewModel = cartViewModel)
    }

    composable(
        route = "${Routes.CHECKOUT}/{itemIds}/{shipVoucherId}/{discountVoucherId}",
        arguments = listOf(
            navArgument("itemIds") { type = NavType.StringType },
            navArgument("shipVoucherId") { type = NavType.StringType },
            navArgument("discountVoucherId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val itemIdsString = backStackEntry.arguments?.getString("itemIds") ?: ""
        val shipId = backStackEntry.arguments?.getString("shipVoucherId") ?: "none"
        val discountId = backStackEntry.arguments?.getString("discountVoucherId") ?: "none"

        val itemIds = itemIdsString.split(",").filter { it.isNotBlank() }
        val finalShipId = if (shipId == "none") null else shipId
        val finalDiscountId = if (discountId == "none") null else discountId

        val checkoutViewModel: CheckoutViewModel = hiltViewModel()
        CheckoutScreen(
            navController = navController, viewModel = checkoutViewModel,
            selectedItemIds = itemIds, freeshipVoucherId = finalShipId, discountVoucherId = finalDiscountId
        )
    }

    composable(Routes.EDIT_PROFILE) {
        val profileViewModel: ProfileViewModel = hiltViewModel()
        EditProfileScreen(navController = navController, viewModel = profileViewModel)
    }

    composable(Routes.ADDRESS) {
        val addressViewModel: AddressViewModel = hiltViewModel()
        AddressScreen(navController = navController, viewModel = addressViewModel, isSelectionMode = false)
    }

    composable("address_selection") {
        val addressViewModel: AddressViewModel = hiltViewModel()
        AddressScreen(navController = navController, viewModel = addressViewModel, isSelectionMode = true)
    }

    composable(Routes.MY_VOUCHER) {
        val myVoucherViewModel: MyVoucherViewModel = hiltViewModel()
        MyVoucherScreen(navController = navController, viewModel = myVoucherViewModel)
    }

    composable(Routes.VOUCHER_HISTORY) {
        val myVoucherViewModel: MyVoucherViewModel = hiltViewModel()
        com.example.handispace.ui.user.profile.myvoucher.VoucherHistoryScreen(
            navController = navController,
            viewModel = myVoucherViewModel
        )
    }

    composable(Routes.LOYALTY_SCREEN) {
        val profileViewModel: ProfileViewModel = hiltViewModel()
        com.example.handispace.ui.user.profile.LoyaltyScreen(
            navController = navController,
            viewModel = profileViewModel
        )
    }

    composable(
        route = "${Routes.ORDER_DETAIL}/{orderId}",
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        val viewModel: OrderDetailViewModel = hiltViewModel()
        OrderDetailScreen(
            navController = navController,
            orderId = orderId,
            viewModel = viewModel
        )
    }

    composable(
        route = "${Routes.REVIEW_ORDER}/{orderId}",
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        val viewModel: com.example.handispace.ui.user.review.ReviewOrderViewModel = hiltViewModel()
        com.example.handispace.ui.user.review.ReviewOrderScreen(
            navController = navController, orderId = orderId, viewModel = viewModel
        )
    }

    // 🔥 ĐÃ PHỤC HỒI: Cổng Chat để Khách Hàng (User) có thể vào được
    composable(
        route = "chat/{targetUserId}/{targetUserName}?context={context}",
        arguments = listOf(
            navArgument("targetUserId") { type = NavType.StringType },
            navArgument("targetUserName") { type = NavType.StringType },
            navArgument("context") { type = NavType.StringType; nullable = true; defaultValue = null }
        )
    ) { backStackEntry ->
        val targetUserId = backStackEntry.arguments?.getString("targetUserId") ?: ""
        val targetUserName = backStackEntry.arguments?.getString("targetUserName") ?: "Chat"
        val contextMsg = backStackEntry.arguments?.getString("context")

        val chatViewModel: com.example.handispace.ui.chat.ChatViewModel = hiltViewModel()

        com.example.handispace.ui.chat.ChatScreen(
            navController = navController,
            viewModel = chatViewModel,
            targetUserId = targetUserId,
            targetUserName = targetUserName,
            initialContext = contextMsg
        )
    }
    composable(Routes.AI_CHAT) {
        AiChatScreen(navController = navController)
    }
}

fun NavGraphBuilder.adminGraph(navController: NavController) {
    composable(Routes.ADMIN_DASHBOARD) {
        com.example.handispace.ui.admin.AdminLayout(mainNavController = navController)
    }

    composable(Routes.ADMIN_CHAT) {
        ChatListScreen(navController = navController)
    }

    composable(Routes.ADMIN_ADD_PRODUCT) {
        com.example.handispace.ui.admin.products.AdminAddProductScreen(navController = navController)
    }

    composable(
        route = "${Routes.ADMIN_EDIT_PRODUCT}/{productId}",
        arguments = listOf(navArgument("productId") { type = NavType.StringType })
    ) { backStackEntry ->
        val productId = backStackEntry.arguments?.getString("productId") ?: ""
        com.example.handispace.ui.admin.products.AdminEditProductScreen(
            navController = navController,
            productId = productId
        )
    }

    composable(
        route = "${Routes.ADMIN_ORDER_DETAIL}/{orderId}",
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        com.example.handispace.ui.admin.orders.AdminOrderDetailScreen(
            navController = navController,
            orderId = orderId
        )
    }

    composable(Routes.ADMIN_ADD_CATEGORY) {
        com.example.handispace.ui.admin.categories.AdminAddEditCategoryScreen(
            navController = navController,
            categoryId = null
        )
    }

    composable(
        route = "${Routes.ADMIN_EDIT_CATEGORY}/{categoryId}",
        arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
    ) { backStackEntry ->
        val categoryId = backStackEntry.arguments?.getString("categoryId")
        com.example.handispace.ui.admin.categories.AdminAddEditCategoryScreen(
            navController = navController,
            categoryId = categoryId
        )
    }

    composable(Routes.ADMIN_CATEGORIES) {
        com.example.handispace.ui.admin.categories.AdminCategoriesScreen(navController = navController)
    }

    composable(
        route = "${Routes.ADMIN_USER_DETAIL}/{userId}",
        arguments = listOf(navArgument("userId") { type = NavType.StringType })
    ) { backStackEntry ->
        val userId = backStackEntry.arguments?.getString("userId") ?: ""
        com.example.handispace.ui.admin.users.AdminUserDetailScreen(
            navController = navController,
            userId = userId
        )
    }

    composable(Routes.ADMIN_VOUCHERS) {
        com.example.handispace.ui.admin.vouchers.AdminVouchersScreen(navController = navController)
    }

    composable(Routes.ADMIN_ADD_VOUCHER) {
        com.example.handispace.ui.admin.vouchers.AdminAddEditVoucherScreen(navController = navController, voucherId = null)
    }

    composable(
        route = "${Routes.ADMIN_EDIT_VOUCHER}/{voucherId}",
        arguments = listOf(navArgument("voucherId") { type = NavType.StringType })
    ) { backStackEntry ->
        val voucherId = backStackEntry.arguments?.getString("voucherId")
        com.example.handispace.ui.admin.vouchers.AdminAddEditVoucherScreen(navController = navController, voucherId = voucherId)
    }
}
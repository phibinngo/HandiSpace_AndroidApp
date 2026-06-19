package com.example.handispace.ui.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.handispace.ui.user.profile.ProfileScreen
import com.example.handispace.ui.user.profile.ProfileViewModel
import com.example.handispace.ui.user.shop.ShopScreen
import com.example.handispace.ui.user.shop.ShopViewModel
import com.example.handispace.ui.user.voucher.VoucherScreen
import com.example.handispace.ui.user.voucher.UserVoucherViewModel
import com.example.handispace.ui.user.order.history.OrderHistoryScreen
import com.example.handispace.ui.user.order.history.OrderHistoryViewModel
import com.example.handispace.ui.user.notification.NotificationScreen
import com.example.handispace.ui.user.notification.NotificationViewModel

sealed class BottomTab(val route: String, val icon: ImageVector, val title: String) {
    object Shop : BottomTab("tab_shop", Icons.Filled.Home, "Trang chủ")
    object Voucher : BottomTab("tab_voucher", Icons.Filled.LocalOffer, "Voucher")
    object Order : BottomTab("tab_order", Icons.Filled.Assignment, "Đơn hàng")
    object Notifications : BottomTab("tab_noti", Icons.Filled.Notifications, "Thông báo")
    object Profile : BottomTab("tab_profile", Icons.Filled.Person, "Tôi")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserLayout(mainNavController: NavController) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shopViewModel: ShopViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val orderHistoryViewModel: OrderHistoryViewModel = hiltViewModel()
    val voucherViewModel: UserVoucherViewModel = hiltViewModel()
    val notiViewModel: NotificationViewModel = hiltViewModel()

    val unreadCount by notiViewModel.unreadCount.collectAsState()

    val tabs = listOf(BottomTab.Shop, BottomTab.Voucher, BottomTab.Order, BottomTab.Notifications, BottomTab.Profile)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color(0xFFEA580C),
                tonalElevation = 8.dp
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        icon = {
                            if (tab.route == BottomTab.Notifications.route && unreadCount > 0) {
                                BadgedBox(badge = { Badge { Text(unreadCount.toString()) } }) {
                                    Icon(imageVector = tab.icon, contentDescription = tab.title, modifier = Modifier.size(24.dp))
                                }
                            } else {
                                Icon(imageVector = tab.icon, contentDescription = tab.title, modifier = Modifier.size(24.dp))
                            }
                        },
                        label = { Text(text = tab.title, fontSize = 10.sp, maxLines = 1, textAlign = TextAlign.Center) },
                        selected = currentRoute == tab.route,
                        alwaysShowLabel = true,
                        onClick = {
                            when (tab.route) {
                                BottomTab.Shop.route -> shopViewModel.refreshData()
                                BottomTab.Profile.route -> profileViewModel.loadUserProfile()
                                BottomTab.Order.route -> orderHistoryViewModel.loadMyOrders()
                                BottomTab.Voucher.route -> voucherViewModel.loadVouchers()
                                BottomTab.Notifications.route -> notiViewModel.loadNotifications()
                            }

                            if (currentRoute != tab.route) {
                                bottomNavController.navigate(tab.route) {
                                    popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFEA580C),
                            selectedTextColor = Color(0xFFEA580C),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.White
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomTab.Shop.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomTab.Shop.route) {
                ShopScreen(navController = mainNavController, viewModel = shopViewModel)
            }
            composable(BottomTab.Voucher.route) {
                VoucherScreen(viewModel = voucherViewModel)
            }
            composable(BottomTab.Order.route) {
                OrderHistoryScreen(navController = mainNavController, viewModel = orderHistoryViewModel)
            }
            composable(BottomTab.Notifications.route) {
                NotificationScreen(viewModel = notiViewModel)
            }
            composable(BottomTab.Profile.route) {
                ProfileScreen(navController = mainNavController, viewModel = profileViewModel)
            }
        }
    }
}
package com.example.handispace.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.handispace.ui.admin.dashboard.AdminDashboardScreen
import com.example.handispace.ui.admin.dashboard.AdminDashboardViewModel
import com.example.handispace.ui.admin.orders.AdminOrderViewModel
import com.example.handispace.ui.admin.products.AdminProductViewModel
import com.example.handispace.ui.admin.users.AdminUserViewModel
// 🔥 IMPORT THÊM VIEWMODEL CHAT VÀO ĐÂY
import com.example.handispace.ui.chat.ChatListViewModel

sealed class AdminBottomTab(val route: String, val icon: ImageVector, val title: String) {
    object Dashboard : AdminBottomTab("admin_tab_dashboard", Icons.Filled.Dashboard, "Tổng quan")
    object Orders : AdminBottomTab("admin_tab_orders", Icons.Filled.Assignment, "Đơn hàng")
    object Products : AdminBottomTab("admin_tab_products", Icons.Filled.Inventory, "Sản phẩm")
    object Users : AdminBottomTab("admin_tab_users", Icons.Filled.People, "Khách hàng")
    object More : AdminBottomTab("admin_tab_more", Icons.Filled.Menu, "Thêm")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLayout(mainNavController: NavController) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val dashboardViewModel: AdminDashboardViewModel = hiltViewModel()
    val orderViewModel: AdminOrderViewModel = hiltViewModel()
    val productViewModel: AdminProductViewModel = hiltViewModel()
    val userViewModel: AdminUserViewModel = hiltViewModel()

    // 🔥 GỌI CHAT VIEWMODEL ĐỂ LẤY SỐ UNREAD THẬT
    val chatListViewModel: ChatListViewModel = hiltViewModel()
    val unreadChatCount = chatListViewModel.totalUnreadCount.value

    val orange600 = Color(0xFFEA580C)

    val tabs = listOf(
        AdminBottomTab.Dashboard,
        AdminBottomTab.Orders,
        AdminBottomTab.Products,
        AdminBottomTab.Users,
        AdminBottomTab.More
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản Trị HandiSpace", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)) },
                actions = {
                    IconButton(onClick = { mainNavController.navigate("admin_chat") }) {
                        if (unreadChatCount > 0) {
                            BadgedBox(badge = { Badge(containerColor = Color.Red) { Text(unreadChatCount.toString(), color = Color.White) } }) {
                                Icon(Icons.Outlined.Chat, contentDescription = "Chat", tint = orange600)
                            }
                        } else {
                            Icon(Icons.Outlined.Chat, contentDescription = "Chat", tint = orange600)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, contentColor = orange600, tonalElevation = 8.dp) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.title, modifier = Modifier.size(24.dp)) },
                        label = { Text(text = tab.title, fontSize = 10.sp, maxLines = 1, textAlign = TextAlign.Center) },
                        selected = currentRoute == tab.route,
                        alwaysShowLabel = true,
                        onClick = {
                            when (tab.route) {
                                AdminBottomTab.Dashboard.route -> dashboardViewModel.loadDashboardData()
                                AdminBottomTab.Products.route -> productViewModel.loadData()
                                AdminBottomTab.Users.route -> userViewModel.loadAllUsers()
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
                            selectedIconColor = orange600,
                            selectedTextColor = orange600,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.White
                        )
                    )
                }
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = AdminBottomTab.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AdminBottomTab.Dashboard.route) { AdminDashboardScreen(navController = mainNavController, viewModel = dashboardViewModel) }
            composable(AdminBottomTab.Orders.route) { com.example.handispace.ui.admin.orders.AdminOrdersScreen(navController = mainNavController, viewModel = orderViewModel) }
            composable(AdminBottomTab.Products.route) { com.example.handispace.ui.admin.products.AdminProductsScreen(navController = mainNavController, viewModel = productViewModel) }
            composable(AdminBottomTab.Users.route) { com.example.handispace.ui.admin.users.AdminUsersScreen(navController = mainNavController, viewModel = userViewModel) }
            composable(AdminBottomTab.More.route) { com.example.handispace.ui.admin.menu.AdminMenuScreen(navController = mainNavController) }
        }
    }
}
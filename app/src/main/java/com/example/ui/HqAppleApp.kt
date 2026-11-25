package com.example.ui

import android.content.Intent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.ui.screens.AllOrdersScreen
import com.example.ui.screens.HqSettingsScreen
import com.example.ui.screens.QuickSaleScreen
import com.example.ui.screens.RepairIntakeScreen
import com.example.ui.theme.HqAppleTheme
import com.khohangmin.KhoHangMinActivity
import com.shopcore.sync.ShopSyncUiState
import com.shopcore.ui.RenderAuthGateScreen

private enum class HqTab(
  val route: String,
  val label: String,
  val title: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
) {
  REPAIR("hq_repair", "Tiếp nhận sửa", "HQ Apple · Phiếu sửa chữa", Icons.Filled.Build, Icons.Outlined.Build),
  QUICK_SALE("hq_quick_sale", "Bán nhanh", "Bán nhanh Phụ kiện & Cường lực", Icons.Filled.FlashOn, Icons.Outlined.FlashOn),
  ORDERS("hq_orders", "Đơn hàng", "Đơn bán ra & Sửa chữa", Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong),
  SETTINGS("hq_settings", "Đồng bộ", "Sao lưu .hqbak & Render", Icons.Filled.CloudSync, Icons.Outlined.CloudSync),
}

@Composable
fun HqAppleApp(
  viewModel: HqAppleViewModel,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val syncState by viewModel.syncUiState.collectAsStateWithLifecycle()
  val navController = rememberNavController()
  val snackbarHostState = remember { SnackbarHostState() }
  val context = LocalContext.current

  LaunchedEffect(uiState.message) {
    val msg = uiState.message
    if (msg != null) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearMessage()
    }
  }

  val openKhoHangMin = remember(context) {
    {
      context.startActivity(Intent(context, KhoHangMinActivity::class.java))
    }
  }

  HqAppleTheme(darkTheme = uiState.darkTheme) {
    if (!syncState.session.isLoggedIn && !syncState.offlineBypass) {
      RenderAuthGateScreen(
        appTitle = "HQ Apple",
        appSubtitle = "Đăng nhập / Đăng ký Server Render để đồng bộ cùng Kho Hàng Min",
        heroImageRes = R.drawable.img_hq_apple_hero,
        syncState = syncState,
        syncManager = viewModel.syncManager,
        snackbarHostState = snackbarHostState,
      )
    } else {
      BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 700.dp
        HqAppleScaffold(
          useRail = useRail,
          uiState = uiState,
          syncState = syncState,
          navController = navController,
          snackbarHostState = snackbarHostState,
          viewModel = viewModel,
          onOpenKhoHangMin = openKhoHangMin,
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HqAppleScaffold(
  useRail: Boolean,
  uiState: HqAppleUiState,
  syncState: ShopSyncUiState,
  navController: NavHostController,
  snackbarHostState: SnackbarHostState,
  viewModel: HqAppleViewModel,
  onOpenKhoHangMin: () -> Unit,
) {
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route ?: HqTab.REPAIR.route
  val currentTab = HqTab.entries.find { it.route == currentRoute } ?: HqTab.REPAIR

  val navigateToTab = remember(navController) {
    { tab: HqTab ->
      navController.navigate(tab.route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
      }
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    contentWindowInsets = WindowInsets.systemBars,
    topBar = {
      TopAppBar(
        title = { Text(currentTab.title, style = MaterialTheme.typography.titleLarge) },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
          ),
      )
    },
    bottomBar = {
      if (!useRail) {
        NavigationBar {
          HqTab.entries.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
              selected = selected,
              onClick = { navigateToTab(tab) },
              icon = {
                HqNavIcon(
                  tab = tab,
                  selected = selected,
                  cartItemCount = uiState.cartItemCount,
                )
              },
              label = { Text(tab.label, maxLines = 1) },
              modifier = Modifier.testTag("hq_nav_${tab.route}"),
            )
          }
        }
      }
    },
  ) { innerPadding ->
    Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      if (useRail) {
        NavigationRail {
          HqTab.entries.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationRailItem(
              selected = selected,
              onClick = { navigateToTab(tab) },
              icon = {
                HqNavIcon(
                  tab = tab,
                  selected = selected,
                  cartItemCount = uiState.cartItemCount,
                )
              },
              label = { Text(tab.label, maxLines = 1) },
            )
          }
        }
      }

      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
      ) {
        NavHost(
          navController = navController,
          startDestination = HqTab.REPAIR.route,
          enterTransition = { EnterTransition.None },
          exitTransition = { ExitTransition.None },
          popEnterTransition = { EnterTransition.None },
          popExitTransition = { ExitTransition.None },
          modifier = Modifier.widthIn(max = 760.dp).fillMaxSize(),
        ) {
          composable(HqTab.REPAIR.route) {
            RepairIntakeScreen(
              uiState = uiState,
              onSearchChange = viewModel::setRepairSearch,
              onStatusFilterChange = viewModel::setRepairStatusFilter,
              onCreateTicket = viewModel::createRepairTicket,
              onUpdateStatus = viewModel::updateRepairStatus,
            )
          }
          composable(HqTab.QUICK_SALE.route) {
            QuickSaleScreen(
              uiState = uiState,
              onSearchChange = viewModel::setQuickSaleSearch,
              onCategoryChange = viewModel::setQuickSaleCategory,
              onAddToCart = viewModel::addToCart,
              onDecrementCart = viewModel::decrementCart,
              onClearCart = viewModel::clearCart,
              onCheckout = viewModel::checkoutQuickSale,
            )
          }
          composable(HqTab.ORDERS.route) {
            AllOrdersScreen(
              uiState = uiState,
              onSearchChange = viewModel::setOrderSearch,
              onTypeFilterChange = viewModel::setOrderTypeFilter,
            )
          }
          composable(HqTab.SETTINGS.route) {
            HqSettingsScreen(
              uiState = uiState,
              syncState = syncState,
              syncManager = viewModel.syncManager,
              onToggleDarkTheme = viewModel::toggleDarkTheme,
              onOpenKhoHangMin = onOpenKhoHangMin,
              onClearAllData = viewModel::clearAllData,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun HqNavIcon(
  tab: HqTab,
  selected: Boolean,
  cartItemCount: Int,
) {
  val icon = if (selected) tab.selectedIcon else tab.unselectedIcon
  if (tab == HqTab.QUICK_SALE && cartItemCount > 0) {
    BadgedBox(
      badge = { Badge { Text(cartItemCount.toString()) } },
    ) {
      Icon(imageVector = icon, contentDescription = tab.label)
    }
  } else {
    Icon(imageVector = icon, contentDescription = tab.label)
  }
}

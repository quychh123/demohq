package com.khohangmin.ui

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Inventory2
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
import com.khohangmin.R
import com.khohangmin.ui.screens.KhoCashbookScreen
import com.khohangmin.ui.screens.KhoCustomersScreen
import com.khohangmin.ui.screens.KhoDashboardScreen
import com.khohangmin.ui.screens.KhoGoodsScreen
import com.khohangmin.ui.screens.KhoSyncScreen
import com.khohangmin.ui.theme.KhoHangMinTheme
import com.shopcore.sync.ShopSyncUiState
import com.shopcore.ui.RenderAuthGateScreen

private enum class KhoTab(
  val route: String,
  val label: String,
  val title: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
) {
  DASHBOARD("kho_dashboard", "Tổng quan", "Kho Hàng Min · Tổng quan", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
  GOODS("kho_goods", "Hàng hóa", "Kho · Phụ kiện & Linh kiện", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
  CASHBOOK("kho_cashbook", "Sổ quỹ", "Sổ quỹ · Gốc & Lời", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
  CUSTOMERS("kho_customers", "Khách hàng", "Khách hàng đồng bộ", Icons.Filled.Group, Icons.Outlined.Group),
  SYNC("kho_sync", "Đồng bộ", "Sao lưu .hqbak & Render", Icons.Filled.CloudSync, Icons.Outlined.CloudSync),
}

@Composable
fun KhoHangMinApp(
  viewModel: WarehouseViewModel,
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

  val openHqApple = remember(context) {
    {
      try {
        val mainActivityClass = Class.forName("com.example.MainActivity")
        context.startActivity(Intent(context, mainActivityClass))
      } catch (_: ClassNotFoundException) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.aistudio.hqapple.xkmpqz")
        if (launchIntent != null) {
          context.startActivity(launchIntent)
        } else {
          Toast.makeText(context, "Chưa cài đặt ứng dụng HQ Apple trên máy", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  KhoHangMinTheme(darkTheme = uiState.darkTheme) {
    if (!syncState.session.isLoggedIn && !syncState.offlineBypass) {
      RenderAuthGateScreen(
        appTitle = "Kho Hàng Min",
        appSubtitle = "Đăng nhập / Đăng ký Server Render để đồng bộ cùng HQ Apple",
        heroImageRes = R.drawable.img_kho_min_hero,
        syncState = syncState,
        syncManager = viewModel.syncManager,
        snackbarHostState = snackbarHostState,
      )
    } else {
      BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 700.dp
        KhoHangMinScaffold(
          useRail = useRail,
          uiState = uiState,
          syncState = syncState,
          navController = navController,
          snackbarHostState = snackbarHostState,
          viewModel = viewModel,
          onOpenHqApple = openHqApple,
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KhoHangMinScaffold(
  useRail: Boolean,
  uiState: WarehouseUiState,
  syncState: ShopSyncUiState,
  navController: NavHostController,
  snackbarHostState: SnackbarHostState,
  viewModel: WarehouseViewModel,
  onOpenHqApple: () -> Unit,
) {
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route ?: KhoTab.DASHBOARD.route
  val currentTab = KhoTab.entries.find { it.route == currentRoute } ?: KhoTab.DASHBOARD

  val navigateToTab = remember(navController) {
    { tab: KhoTab ->
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
          KhoTab.entries.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
              selected = selected,
              onClick = { navigateToTab(tab) },
              icon = {
                Icon(
                  imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                  contentDescription = tab.label,
                )
              },
              label = { Text(tab.label, maxLines = 1) },
              modifier = Modifier.testTag("kho_nav_${tab.route}"),
            )
          }
        }
      }
    },
  ) { innerPadding ->
    Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      if (useRail) {
        NavigationRail {
          KhoTab.entries.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationRailItem(
              selected = selected,
              onClick = { navigateToTab(tab) },
              icon = {
                Icon(
                  imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                  contentDescription = tab.label,
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
          startDestination = KhoTab.DASHBOARD.route,
          enterTransition = { EnterTransition.None },
          exitTransition = { ExitTransition.None },
          popEnterTransition = { EnterTransition.None },
          popExitTransition = { ExitTransition.None },
          modifier = Modifier.widthIn(max = 760.dp).fillMaxSize(),
        ) {
          composable(KhoTab.DASHBOARD.route) {
            KhoDashboardScreen(
              uiState = uiState,
              onQuickRestock = viewModel::adjustStock,
              onNavigateToCategory = { subCat ->
                viewModel.setGoodsFilter(subCat)
                navigateToTab(KhoTab.GOODS)
              },
              onNavigateToCashbook = { navigateToTab(KhoTab.CASHBOOK) },
            )
          }
          composable(KhoTab.GOODS.route) {
            KhoGoodsScreen(
              uiState = uiState,
              onSearchChange = viewModel::setGoodsSearch,
              onFilterChange = viewModel::setGoodsFilter,
              onAdjustStock = viewModel::adjustStock,
              onSaveProduct = viewModel::saveProduct,
              onDeleteProduct = viewModel::deleteProduct,
            )
          }
          composable(KhoTab.CASHBOOK.route) {
            KhoCashbookScreen(
              uiState = uiState,
              onFilterChange = viewModel::setCashbookFilter,
              onAddManualEntry = viewModel::addManualCashbookEntry,
            )
          }
          composable(KhoTab.CUSTOMERS.route) {
            KhoCustomersScreen(
              uiState = uiState,
              onSearchChange = viewModel::setCustomerSearch,
              onSaveCustomer = viewModel::saveCustomer,
              onDeleteCustomer = viewModel::deleteCustomer,
              onToggleDarkTheme = viewModel::toggleDarkTheme,
              onOpenHqApple = onOpenHqApple,
            )
          }
          composable(KhoTab.SYNC.route) {
            KhoSyncScreen(
              uiState = uiState,
              syncState = syncState,
              syncManager = viewModel.syncManager,
              productsCount = uiState.products.size,
              customersCount = uiState.customers.size,
              repairsCount = uiState.repairsCount,
              ordersCount = uiState.ordersCount,
              cashbookCount = uiState.cashbookEntries.size,
            )
          }
        }
      }
    }
  }
}

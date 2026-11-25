package com.khohangmin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khohangmin.ui.KhoHangMinApp
import com.khohangmin.ui.WarehouseViewModel
import com.shopcore.data.ShopDatabase
import com.shopcore.data.ShopRepository

open class KhoHangMinActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val repository = ShopRepository(ShopDatabase.getInstance(applicationContext))
    setContent {
      val warehouseViewModel: WarehouseViewModel =
        viewModel(factory = WarehouseViewModel.Factory(applicationContext, repository))
      KhoHangMinApp(viewModel = warehouseViewModel)
    }
  }
}

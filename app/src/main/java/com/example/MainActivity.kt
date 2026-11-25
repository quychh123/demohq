package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.HqAppleApp
import com.example.ui.HqAppleViewModel
import com.shopcore.data.ShopDatabase
import com.shopcore.data.ShopRepository

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val repository = ShopRepository(ShopDatabase.getInstance(applicationContext))
    setContent {
      val hqAppleViewModel: HqAppleViewModel =
        viewModel(factory = HqAppleViewModel.Factory(applicationContext, repository))
      HqAppleApp(viewModel = hqAppleViewModel)
    }
  }
}

package com.nesrine.smartshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.google.firebase.auth.FirebaseAuth
import com.nesrine.smartshop.data.local.AppDatabase
import com.nesrine.smartshop.data.remote.FirestoreProductDataSource
import com.nesrine.smartshop.data.repository.ProductRepository
import com.nesrine.smartshop.ui.auth.LoginScreen
import com.nesrine.smartshop.ui.products.ProductViewModel
import com.nesrine.smartshop.ui.products.ProductsScreen
import com.nesrine.smartshop.ui.theme.SmartShopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(this)
        val remote = FirestoreProductDataSource()
        val repository = ProductRepository(db.productDao(), remote)

        setContent {
            SmartShopTheme {
                var loggedIn by rememberSaveable {
                    mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
                }

                val productViewModel = remember { ProductViewModel(repository) }

                if (loggedIn) {
                    ProductsScreen(viewModel = productViewModel)
                } else {
                    LoginScreen(onLoginSuccess = { loggedIn = true })
                }
            }
        }
    }
}

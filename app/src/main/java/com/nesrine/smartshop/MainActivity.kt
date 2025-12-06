package com.nesrine.smartshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.nesrine.smartshop.home.HomeScreen
import com.nesrine.smartshop.ui.theme.SmartShopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SmartShopTheme {
                var loggedIn by remember { mutableStateOf(false) }

                if (loggedIn) {
                    HomeScreen()
                } else {
                    LoginScreen(onLoginSuccess = { loggedIn = true })
                }
            }
        }
    }
}

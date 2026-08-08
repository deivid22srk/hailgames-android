package com.hailgames.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hailgames.app.ui.navigation.HailgamesNavHost
import com.hailgames.app.ui.theme.HailgamesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HailgamesTheme {
                HailgamesNavHost()
            }
        }
    }
}

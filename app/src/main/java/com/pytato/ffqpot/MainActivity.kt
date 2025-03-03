package com.pytato.ffqpot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pytato.ffqpot.ui.navigation.AppNavigation
import com.pytato.ffqpot.ui.theme.FFqPotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FFqPotTheme {
                AppNavigation()
            }
        }
    }
}
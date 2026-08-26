package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.MainAppScaffold
import com.example.ui.theme.SistemPpiTheme
import com.example.ui.viewmodel.PpiViewModel

class MainActivity : ComponentActivity() {
    private val ppiViewModel: PpiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SistemPpiTheme {
                MainAppScaffold(viewModel = ppiViewModel)
            }
        }
    }
}

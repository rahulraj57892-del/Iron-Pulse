package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.IronPulseAppContent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GymViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        // Instantiate our single-source-of-truth Jetpack Compose ViewModel
        val gymViewModel: GymViewModel = viewModel()
        IronPulseAppContent(viewModel = gymViewModel)
      }
    }
  }
}

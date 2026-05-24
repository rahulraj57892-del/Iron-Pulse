package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = NeonRed,
  secondary = ElectricBlue,
  tertiary = Color(0xFFFFB300),
  background = CarbonBlack,
  surface = CarbonDark,
  surfaceVariant = CarbonCard,
  onPrimary = Color.White,
  onSecondary = CarbonBlack,
  onBackground = TextPrimary,
  onSurface = TextPrimary,
  onSurfaceVariant = TextSecondary,
  outline = CarbonBorder
)

@Composable
fun IronPulseTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}

// Fallback alias for backward compatibility or existing templates
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  IronPulseTheme(content = content)
}

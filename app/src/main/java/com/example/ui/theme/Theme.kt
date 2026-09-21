package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = AccentCyan,
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF004F5C),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = GeminiPurple,
    onSecondary = Color(0xFF2E0065),
    secondaryContainer = Color(0xFF451A75),
    onSecondaryContainer = Color(0xFFEADBFF),
    tertiary = AccentGold,
    background = TradingDarkBg,
    onBackground = Color(0xFFF1F5F9),
    surface = TradingDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = TradingDarkCard,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = TradingDarkBorder
  )

private val LightColorScheme =
  darkColorScheme(
    primary = AccentCyan,
    onPrimary = Color(0xFF00363F),
    background = TradingDarkBg,
    onBackground = Color(0xFFF1F5F9),
    surface = TradingDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = TradingDarkCard,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = TradingDarkBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

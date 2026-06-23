package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ChampagneGold,
    secondary = LightCasinoGreen,
    tertiary = BrightGold,
    background = DarkCharcoal,
    surface = CardSlate,
    onPrimary = DarkCharcoal,
    onSecondary = TextLight,
    onBackground = TextLight,
    onSurface = TextLight
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ChampagneGold,
    secondary = LightCasinoGreen,
    tertiary = BrightGold,
    background = DarkCharcoal, // Immersive design: Casino apps stay dark even in light mode
    surface = CardSlate,
    onPrimary = DarkCharcoal,
    onSecondary = TextLight,
    onBackground = TextLight,
    onSurface = TextLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for the high-end casino table atmosphere
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve our hand-crafted luxury palette
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

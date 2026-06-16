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

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
  )

private val HighContrastDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFF00), // High Contrast Yellow
    onPrimary = Color.Black,
    secondary = Color(0xFF00FFFF), // High Contrast Cyan
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1C1C1C),
    onSurfaceVariant = Color.White,
    outline = Color.White,
    outlineVariant = Color.White
)

private val HighContrastLightColorScheme = lightColorScheme(
    primary = Color(0xFF0000FF), // High Contrast Pure Blue
    onPrimary = Color.White,
    secondary = Color(0xFF006600), // High Contrast Dark Green
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF2F2F2),
    onSurfaceVariant = Color.Black,
    outline = Color.Black,
    outlineVariant = Color.Black
)

@Composable
fun MyApplicationTheme(
  themeMode: String = "system",
  contrastMode: String = "normal",
  fontScale: Float = 1.0f,
  content: @Composable () -> Unit,
) {
  val darkTheme = when (themeMode) {
    "light" -> false
    "dark" -> true
    else -> isSystemInDarkTheme()
  }

  val colorScheme = when {
    contrastMode == "high" -> {
      if (darkTheme) HighContrastDarkColorScheme else HighContrastLightColorScheme
    }
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  val scaledTypography = getScaledTypography(fontScale)

  MaterialTheme(colorScheme = colorScheme, typography = scaledTypography, content = content)
}

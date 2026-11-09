package com.st.sotan.amarmaheshkhali.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalLang = compositionLocalOf { "bn" }

/** Pick Bangla or English text depending on the app language. */
@Composable
fun tr(bn: String, en: String): String = if (LocalLang.current == "bn") bn else en

private val Green = Color(0xFF1B7F4B)
private val GreenDark = Color(0xFF0F5A34)
private val GreenSoft = Color(0xFFE3F4EA)

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = GreenSoft,
    onPrimaryContainer = GreenDark,
    secondary = Color(0xFF3F8F66),
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F7F3)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6CCB9A),
    onPrimary = Color(0xFF003920),
    primaryContainer = Color(0xFF14432B),
    onPrimaryContainer = Color(0xFFCDEFDB),
    secondary = Color(0xFF8BD0AC),
    background = Color(0xFF0E1511),
    surface = Color(0xFF0E1511),
    surfaceVariant = Color(0xFF1A241F)
)

@Composable
fun AppTheme(mode: String, content: @Composable () -> Unit) {
    val dark = when (mode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}

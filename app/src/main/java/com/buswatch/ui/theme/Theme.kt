// ABOUTME: Main theme composable managing light/dark theme colors
package com.buswatch.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

@Composable
fun BusWatchTheme(
    content: @Composable () -> Unit
) {
    val colors = Colors(
        primary = DarkPrimary,
        primaryVariant = DarkPrimary,
        secondary = LiveGreen,
        secondaryVariant = LiveGreen,
        background = DarkBackground,
        surface = DarkBackground,
        error = Color(0xFFCF6679),
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
        onError = Color.Black
    )

    MaterialTheme(
        colors = colors,
        typography = BusWatchTypography,
        content = content
    )
}

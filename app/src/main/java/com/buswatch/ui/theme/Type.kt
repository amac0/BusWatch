// ABOUTME: Typography definitions for consistent text styling
package com.buswatch.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Typography

val BusWatchTypography = Typography(
    display1 = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    ),
    title1 = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    ),
    body1 = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    ),
    button = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )
)

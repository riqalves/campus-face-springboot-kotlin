package com.campusface.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
@Composable
fun CampusFaceTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorTheme,
        typography = Typography,
        content = content
    )
}
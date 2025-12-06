package com.campusface.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Background = Color(0xFFFFFFFF)
val Primary = Color(0xFF000000)
val Secondary = Color(0xFF848484)
val Tertiary = Color(0xFFA1A1A1)// cinza pros textos
val PrimaryContainer = Color(0xFFF4F4F4) // cinza claro pros cards

val Surface = Color(0xFF1C1B1F)
val SurfaceContainer = Color.Transparent

val LightColorTheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Tertiary,
    surface = Surface,
    background = Background,
    surfaceContainer = SurfaceContainer,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = Primary
)
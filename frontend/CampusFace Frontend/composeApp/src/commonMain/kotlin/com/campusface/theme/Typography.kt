package com.campusface.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import campusface.composeapp.generated.resources.Inter_Bold
import campusface.composeapp.generated.resources.Inter_Medium
import campusface.composeapp.generated.resources.Inter_SemiBold
import campusface.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.Font

val Inter @Composable get() = FontFamily(
    Font(
        resource = Res.font.Inter_Medium,
        weight = FontWeight.Medium
    ),
    Font(
        resource = Res.font.Inter_SemiBold,
        weight = FontWeight.SemiBold
    ),
    Font(
        resource = Res.font.Inter_Bold,
        weight = FontWeight.Bold
    )
)

val Typography : Typography @Composable get() = Typography(
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp
    ),
)
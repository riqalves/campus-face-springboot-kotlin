package com.campusface.data

import androidx.compose.runtime.staticCompositionLocalOf


val LocalAuthToken = staticCompositionLocalOf<String?> { null }
val LocalUserId = staticCompositionLocalOf<String?> { null }
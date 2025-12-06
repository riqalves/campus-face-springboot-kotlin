package com.campusface

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext

actual fun isCameraSupported(): Boolean {
    return true
}

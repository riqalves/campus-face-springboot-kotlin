package com.campusface

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit

fun main() = application {
    FileKit.init(appId = "MyApplication")
    Window(
        onCloseRequest = ::exitApplication,
        title = "CampusFace",
    ) {
        App()
    }
}
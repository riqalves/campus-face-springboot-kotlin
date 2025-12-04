package com.campusface

import platform.AVFoundation.AVCaptureDevice

actual fun isCameraSupported(): Boolean {
    return AVCaptureDevice.defaultDeviceWithMediaType(mediaType = "vide") != null
}

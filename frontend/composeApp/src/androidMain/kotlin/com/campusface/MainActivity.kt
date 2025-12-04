// composeApp/src/androidMain/java/com/campusface/MainActivity.kt
package com.campusface
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat // 🚨 IMPORTE ISSO!
import com.campusface.theme.CampusFaceTheme

class MainActivity : ComponentActivity() {

    companion object {
        lateinit var pickImageCallback: (ByteArray) -> Unit
        lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) {
                    val bytes = contentResolver.openInputStream(uri)?.readBytes() ?: ByteArray(0)
                    pickImageCallback(bytes)
                }
            }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
                App()
                }
            }
        }


@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
package com.studytracker

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.studytracker.app.navigation.AppNavGraph
import com.studytracker.core.service.StudyAccessibilityService
import com.studytracker.core.ui.theme.StudyTrackerTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkOverlayPermission()
        handleIncomingIntent(intent)

        setContent {
            StudyTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.let { handleIncomingIntent(it) }
    }

    private fun handleIncomingIntent(intent: Intent) {
        val uri: Uri? = intent.data ?: intent.getParcelableExtra(Intent.EXTRA_STREAM) ?: intent.clipData?.getItemAt(0)?.uri
        val extraText: String? = intent.getStringExtra(Intent.EXTRA_TEXT)

        if (uri != null) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                val result = com.studytracker.core.data.package_exchange.StudyPackageExchangeManager.importPackageFromUri(this@MainActivity, uri)
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    result.onSuccess { msg ->
                        android.widget.Toast.makeText(this@MainActivity, "✅ $msg", android.widget.Toast.LENGTH_LONG).show()
                    }.onFailure { err ->
                        android.widget.Toast.makeText(this@MainActivity, "❌ Paket yükleme hatası: ${err.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else if (!extraText.isNullOrBlank() && (extraText.contains("familyCode") || extraText.contains("packageType") || extraText.contains("occurrences"))) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                val result = com.studytracker.core.data.package_exchange.StudyPackageExchangeManager.importPackageString(this@MainActivity, extraText)
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    result.onSuccess { msg ->
                        android.widget.Toast.makeText(this@MainActivity, "✅ $msg", android.widget.Toast.LENGTH_LONG).show()
                    }.onFailure { err ->
                        android.widget.Toast.makeText(this@MainActivity, "❌ Metin yükleme hatası: ${err.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    fun openAccessibilitySettings() {
        StudyAccessibilityService.openAccessibilitySettings(this)
    }
}

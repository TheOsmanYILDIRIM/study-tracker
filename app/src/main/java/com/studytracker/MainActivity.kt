package com.studytracker

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.studytracker.app.navigation.AppNavGraph
import com.studytracker.core.service.StudyAccessibilityService
import com.studytracker.core.ui.theme.StudyTrackerTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private var pendingImportIntent by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkOverlayPermission()
        queueIncomingIntent(intent)

        setContent {
            StudyTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }

                pendingImportIntent?.let { pending ->
                    AlertDialog(
                        onDismissRequest = { pendingImportIntent = null },
                        title = { Text("StudyTracker paketi içe aktarılsın mı?") },
                        text = { Text("Bu dosya veya paylaşılan metin ders planını ve çalışma verilerini değiştirebilir. Yalnız güvendiğiniz kaynaktan geldiyse devam edin.") },
                        confirmButton = {
                            TextButton(onClick = {
                                pendingImportIntent = null
                                importIncomingIntent(pending)
                            }) { Text("İçe Aktar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { pendingImportIntent = null }) { Text("İptal") }
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.let { queueIncomingIntent(it) }
    }

    private fun queueIncomingIntent(intent: Intent) {
        val hasUri = intent.data != null || intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM) != null || intent.clipData?.itemCount?.let { it > 0 } == true
        val hasText = !intent.getStringExtra(Intent.EXTRA_TEXT).isNullOrBlank()
        if (hasUri || hasText) {
            pendingImportIntent = intent
        }
    }

    private fun importIncomingIntent(intent: Intent) {
        val uri: Uri? = intent.data ?: intent.getParcelableExtra(Intent.EXTRA_STREAM) ?: intent.clipData?.getItemAt(0)?.uri
        val extraText: String? = intent.getStringExtra(Intent.EXTRA_TEXT)

        if (uri != null) {
            lifecycleScope.launch(Dispatchers.IO) {
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
            lifecycleScope.launch(Dispatchers.IO) {
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

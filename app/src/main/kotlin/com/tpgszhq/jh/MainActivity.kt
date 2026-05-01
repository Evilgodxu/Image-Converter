package com.tpgszhq.jh

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.tpgszhq.jh.data.repository.UserPreferencesRepository
import com.tpgszhq.jh.ui.adaptive.ProvideWindowSizeClass
import com.tpgszhq.jh.ui.localization.LanguageManager
import com.tpgszhq.jh.ui.localization.ProvideLanguageManager
import com.tpgszhq.jh.ui.navigation.AppNavHost
import com.tpgszhq.jh.ui.navigation.Screen
import com.tpgszhq.jh.ui.theme.ImageConverterTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val userPreferencesRepository: UserPreferencesRepository by inject()
    private lateinit var languageManager: LanguageManager
    private lateinit var windowInsetsController: WindowInsetsControllerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupWindowInsets()

        languageManager = LanguageManager(this, userPreferencesRepository)

        // 获取外部传入的图片URI
        val externalImageUris = extractUrisFromIntent(intent)

        setContent {
            val navController = rememberNavController()
            val themeMode by userPreferencesRepository.themeMode.collectAsState(initial = "system")
            val privacyAccepted by userPreferencesRepository.privacyAccepted.collectAsState(initial = false)

            val language by userPreferencesRepository.language.collectAsState(initial = "system")
            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            var isInitialLaunch by rememberSaveable { mutableStateOf(true) }
            val startDestination = if (isInitialLaunch) {
                isInitialLaunch = false
                if (privacyAccepted) Screen.Home.route else Screen.Privacy.route
            } else {
                null
            }

            ProvideLanguageManager(languageManager) {
                ProvideWindowSizeClass {
                    ImageConverterTheme(darkTheme = darkTheme) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background,
                        ) {
                            if (startDestination != null) {
                                AppNavHost(
                                    navController = navController,
                                    startDestination = startDestination,
                                    onExitApp = { finish() },
                                    externalImageUris = externalImageUris,
                                    language = language,
                                )
                            } else {
                                AppNavHost(
                                    navController = navController,
                                    startDestination = Screen.Home.route,
                                    onExitApp = { finish() },
                                    externalImageUris = externalImageUris,
                                    language = language,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 从Intent中提取URI列表
    private fun extractUrisFromIntent(intent: Intent?): List<Uri> {
        if (intent == null) return emptyList()

        val uris = mutableListOf<Uri>()

        when (intent.action) {
            Intent.ACTION_VIEW -> {
                // 从文件管理器打开单个文件
                intent.data?.let { uris.add(it) }
            }
            Intent.ACTION_SEND -> {
                // 接收分享的单张图片
                (intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))?.let { uris.add(it) }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                // 接收分享的多张图片
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let { uris.addAll(it) }
            }
        }

        return uris
    }

    private fun setupWindowInsets() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        updateSystemBarsVisibility()
    }

    private fun updateSystemBarsVisibility() {
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateSystemBarsVisibility()
    }
}

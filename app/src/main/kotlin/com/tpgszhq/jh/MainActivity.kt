package com.tpgszhq.jh

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tpgszhq.jh.localization.LocalizationManager
import com.tpgszhq.jh.localization.ProvideLocalizedContext
import com.tpgszhq.jh.navigation.AppNavHost
import com.tpgszhq.jh.navigation.Home
import com.tpgszhq.jh.navigation.Privacy
import com.tpgszhq.jh.theme.ImageConverterTheme
import com.tpgszhq.jh.ui.component.dialog.UpdateDialog
import com.tpgszhq.jh.update.AppUpdateViewModel
import com.tpgszhq.jh.update.LocalAppUpdateViewModel

// Activity 只做入口：挂载导航图与全局副作用，不持有状态字段、不参与业务
class MainActivity : ComponentActivity() {

    // 手动 DI：经 Application 容器取依赖，ViewModel 以工厂注入构造参数
    private val appContainer: AppContainer
        get() = (application as App).container

    private val localizationManager: LocalizationManager
        get() = appContainer.localizationManager

    private val mainViewModel: MainViewModel by viewModels {
        viewModelFactory {
            initializer {
                MainViewModel(
                    settingsRepository = appContainer.settingsRepository,
                    appVersion = appContainer.appVersion,
                )
            }
        }
    }

    private val updateViewModel: AppUpdateViewModel by viewModels {
        viewModelFactory {
            initializer {
                AppUpdateViewModel(
                    updateCheckRepository = appContainer.updateCheckRepository,
                    coordinator = appContainer.updateDownloadCoordinator,
                    installer = appContainer.apkInstaller,
                    appVersion = appContainer.appVersion,
                )
            }
        }
    }

    // 外部传入的图片 URI（从文件管理器打开或分享）：冷启动读取一次
    private val externalImageUris: List<Uri> by lazy { extractUrisFromIntent(intent) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // 全局副作用：按窗口方向显隐系统栏
            SystemBarsVisibilityEffect()
            CompositionLocalProvider(
                LocalMainViewModel provides mainViewModel,
                LocalAppUpdateViewModel provides updateViewModel,
                LocalAppContainer provides appContainer,
            ) {
                ProvideLocalizedContext(localizationManager) {
                    MainContent()
                }
            }
        }
    }

    @Composable
    private fun MainContent() {
        ImageConverterTheme {
            val appUiState by mainViewModel.uiState.collectAsStateWithLifecycle()
            val updateUiState by updateViewModel.uiState.collectAsStateWithLifecycle()
            // 回到前台：若仍有待安装的更新包，重新弹出安装确认对话框
            LifecycleResumeEffect(Unit) {
                updateViewModel.onForeground()
                onPauseOrDispose { }
            }
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                // 隐私同意状态未读出前不挂载导航，避免先闪隐私页再跳首页
                val privacyAccepted = appUiState.privacyAccepted
                if (privacyAccepted != null) {
                    AppNavHost(
                        startKey = if (privacyAccepted) Home else Privacy,
                        externalImageUris = externalImageUris,
                        onExitApp = { mainViewModel.clearCacheAndExit(applicationContext) },
                    )
                }
            }
            // 更新对话框挂在应用级：两个页面共用同一份更新状态
            if (updateUiState.dialogVisible) {
                UpdateDialog(
                    state = updateUiState,
                    onDismiss = updateViewModel::dismissDialog,
                    onStartDownload = updateViewModel::startDownload,
                    onRetry = updateViewModel::retryDownload,
                    onInstall = updateViewModel::install,
                )
            }
        }
    }

    // 从 Intent 中提取 URI 列表
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
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)?.let { uris.add(it) }
            }

            Intent.ACTION_SEND_MULTIPLE -> {
                // 接收分享的多张图片
                intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    ?.let { uris.addAll(it) }
            }
        }

        return uris
    }
}

// 按窗口方向显隐系统栏的全局副作用；横屏隐藏、竖屏显示
@Composable
private fun SystemBarsVisibilityEffect() {
    val view = LocalView.current
    if (view.isInEditMode) return
    val orientation = LocalConfiguration.current.orientation
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

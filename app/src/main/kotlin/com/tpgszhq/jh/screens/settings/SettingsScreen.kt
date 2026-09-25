package com.tpgszhq.jh.screens.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tpgszhq.jh.LocalAppContainer
import com.tpgszhq.jh.LocalMainViewModel
import com.tpgszhq.jh.data.settings.AppLanguage
import com.tpgszhq.jh.data.settings.ThemeMode
import com.tpgszhq.jh.screens.settings.compact.SettingsCompactAssembly
import com.tpgszhq.jh.screens.settings.expanded.SettingsExpandedAssembly
import com.tpgszhq.jh.theme.LocalThemeTransitionController
import com.tpgszhq.jh.update.LocalAppUpdateViewModel
import com.tpgszhq.jh.windowsize.WindowSizeClass
import com.tpgszhq.jh.windowsize.rememberWindowSizeClass

// 页面入口：装配状态、按窗口尺寸类分发形态与跨形态副作用，不含布局
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mainViewModel = LocalMainViewModel.current
    val updateViewModel = LocalAppUpdateViewModel.current
    val container = LocalAppContainer.current
    // 页面级 ViewModel：作用域跟随导航条目，随页面出栈回收
    val factory = remember(container) {
        viewModelFactory { initializer { SettingsViewModel(container.settingsRepository) } }
    }
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
    val appUiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val onThemeReveal: (Offset) -> Unit = LocalThemeTransitionController.current::revealAt

    // 输出目录选择：取持久化写权限后落库
    val directoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { treeUri ->
        treeUri?.let { uri ->
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                // 权限获取失败，继续尝试使用
            }
            settingsViewModel.setOutputDirectory(uri)
        }
    }

    // 选中主题：先从点击位置播放圆形揭示动效，再写入主题并关闭弹窗
    val onThemeSelected: (ThemeMode) -> Unit = { mode ->
        onThemeReveal(settingsUiState.pendingThemeClickPosition)
        mainViewModel.setThemeMode(mode)
        settingsViewModel.dismissThemeDialog()
    }
    // 选中语言：写入语言并关闭弹窗
    val onLanguageSelected: (AppLanguage) -> Unit = { language ->
        settingsViewModel.dismissLanguageDialog()
        mainViewModel.setLanguage(language)
    }
    val onSelectOutputDirectory: () -> Unit = { directoryPicker.launch(null) }

    when (rememberWindowSizeClass()) {
        WindowSizeClass.Compact -> SettingsCompactAssembly(
            appUiState = appUiState,
            settingsUiState = settingsUiState,
            updateMessages = updateViewModel.messages,
            onBack = onBack,
            onShowThemeDialog = settingsViewModel::showThemeDialog,
            onDismissThemeDialog = settingsViewModel::dismissThemeDialog,
            onThemeSelected = onThemeSelected,
            onShowLanguageDialog = settingsViewModel::showLanguageDialog,
            onDismissLanguageDialog = settingsViewModel::dismissLanguageDialog,
            onLanguageSelected = onLanguageSelected,
            onSelectOutputDirectory = onSelectOutputDirectory,
            onClearOutputDirectory = settingsViewModel::clearOutputDirectory,
            onCheckForUpdate = updateViewModel::checkNow,
            modifier = modifier,
        )

        WindowSizeClass.Medium, WindowSizeClass.Expanded -> SettingsExpandedAssembly(
            appUiState = appUiState,
            settingsUiState = settingsUiState,
            updateMessages = updateViewModel.messages,
            onBack = onBack,
            onShowThemeDialog = settingsViewModel::showThemeDialog,
            onDismissThemeDialog = settingsViewModel::dismissThemeDialog,
            onThemeSelected = onThemeSelected,
            onShowLanguageDialog = settingsViewModel::showLanguageDialog,
            onDismissLanguageDialog = settingsViewModel::dismissLanguageDialog,
            onLanguageSelected = onLanguageSelected,
            onSelectOutputDirectory = onSelectOutputDirectory,
            onClearOutputDirectory = settingsViewModel::clearOutputDirectory,
            onCheckForUpdate = updateViewModel::checkNow,
            modifier = modifier,
        )
    }
}

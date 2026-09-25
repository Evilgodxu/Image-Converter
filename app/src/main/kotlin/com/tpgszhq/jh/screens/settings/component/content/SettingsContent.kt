package com.tpgszhq.jh.screens.settings.component.content

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tpgszhq.jh.AppUiState
import com.tpgszhq.jh.R
import com.tpgszhq.jh.data.settings.AppLanguage
import com.tpgszhq.jh.data.settings.ThemeMode
import com.tpgszhq.jh.screens.settings.SettingsUiState
import com.tpgszhq.jh.screens.settings.component.appearance.Appearance
import com.tpgszhq.jh.screens.settings.component.dialog.LanguageSelectionDialog
import com.tpgszhq.jh.screens.settings.component.dialog.ThemeSelectionDialog
import com.tpgszhq.jh.screens.settings.component.info.AppInfo
import com.tpgszhq.jh.screens.settings.component.language.Language
import com.tpgszhq.jh.screens.settings.component.output.OutputDirectory
import com.tpgszhq.jh.update.UpdateMessage
import kotlinx.coroutines.flow.Flow

// 页面级组装单元：设置列表 + 弹窗 + 更新检查结果反馈；
// 弹窗显隐由 SettingsViewModel 经 settingsUiState 下传，组件本身无状态
@Composable
fun SettingsContent(
    appUiState: AppUiState,
    settingsUiState: SettingsUiState,
    updateMessages: Flow<UpdateMessage>,
    onShowThemeDialog: (Offset) -> Unit,
    onDismissThemeDialog: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onShowLanguageDialog: () -> Unit,
    onDismissLanguageDialog: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onSelectOutputDirectory: () -> Unit,
    onClearOutputDirectory: () -> Unit,
    onCheckForUpdate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 手动检查更新的一次性提示以 Toast 反馈（已是最新 / 检查失败 / 缺少安装包）
    val context = LocalContext.current
    val upToDateText = stringResource(R.string.settings_update_latest)
    val checkFailedText = stringResource(R.string.settings_update_failed)
    val incompleteReleaseText = stringResource(R.string.settings_update_incomplete)
    // 以文案为键重建收集，避免语言切换后仍提示旧语言文案
    LaunchedEffect(upToDateText, checkFailedText, incompleteReleaseText) {
        updateMessages.collect { message ->
            val text = when (message) {
                UpdateMessage.UpToDate -> upToDateText
                UpdateMessage.CheckFailed -> checkFailedText
                UpdateMessage.IncompleteRelease -> incompleteReleaseText
            }
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Appearance(themeMode = appUiState.themeMode, onThemeClick = onShowThemeDialog)
        Language(language = appUiState.language, onShowDialog = onShowLanguageDialog)
        OutputDirectory(
            outputDirectory = settingsUiState.outputDirectory,
            onSelectDirectory = onSelectOutputDirectory,
            onClearDirectory = onClearOutputDirectory,
        )
        AppInfo(version = appUiState.version, onCheckForUpdate = onCheckForUpdate)
    }

    if (settingsUiState.showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = appUiState.themeMode,
            onDismiss = onDismissThemeDialog,
            onThemeSelected = onThemeSelected,
        )
    }

    if (settingsUiState.showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = appUiState.language,
            onDismiss = onDismissLanguageDialog,
            onLanguageSelected = onLanguageSelected,
        )
    }
}

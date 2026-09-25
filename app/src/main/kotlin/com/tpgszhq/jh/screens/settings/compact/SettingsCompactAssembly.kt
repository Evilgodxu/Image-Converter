package com.tpgszhq.jh.screens.settings.compact

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import com.tpgszhq.jh.AppUiState
import com.tpgszhq.jh.R
import com.tpgszhq.jh.data.settings.AppLanguage
import com.tpgszhq.jh.data.settings.ThemeMode
import com.tpgszhq.jh.screens.settings.SettingsUiState
import com.tpgszhq.jh.screens.settings.component.content.SettingsContent
import com.tpgszhq.jh.ui.component.AppTopBar
import com.tpgszhq.jh.ui.icons.AppIcons
import com.tpgszhq.jh.update.UpdateMessage
import kotlinx.coroutines.flow.Flow

// 设置页窄屏组装器：全宽单列布局
@Composable
fun SettingsCompactAssembly(
    appUiState: AppUiState,
    settingsUiState: SettingsUiState,
    updateMessages: Flow<UpdateMessage>,
    onBack: () -> Unit,
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
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings_title),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ChevronLeft, stringResource(R.string.back))
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        SettingsContent(
            appUiState = appUiState,
            settingsUiState = settingsUiState,
            updateMessages = updateMessages,
            onShowThemeDialog = onShowThemeDialog,
            onDismissThemeDialog = onDismissThemeDialog,
            onThemeSelected = onThemeSelected,
            onShowLanguageDialog = onShowLanguageDialog,
            onDismissLanguageDialog = onDismissLanguageDialog,
            onLanguageSelected = onLanguageSelected,
            onSelectOutputDirectory = onSelectOutputDirectory,
            onClearOutputDirectory = onClearOutputDirectory,
            onCheckForUpdate = onCheckForUpdate,
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .padding(innerPadding),
        )
    }
}

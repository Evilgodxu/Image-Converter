package com.tpgszhq.jh.screens.settings.expanded

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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

// 设置页宽屏组装器：内容限宽居中，避免宽屏下过度拉伸
@Composable
fun SettingsExpandedAssembly(
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .padding(innerPadding),
        ) {
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
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .widthIn(max = 840.dp),
            )
        }
    }
}

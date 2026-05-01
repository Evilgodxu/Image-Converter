package com.tpgszhq.jh.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tpgszhq.jh.R
import com.tpgszhq.jh.ui.adaptive.rememberWindowWidthSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.tpgszhq.jh.ui.localization.stringResource
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit, viewModel: SettingsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val windowWidthSizeClass = rememberWindowWidthSizeClass()

    val context = LocalContext.current

    // 目录选择器
    val directoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { treeUri ->
        treeUri?.let { uri ->
            // 获取持久化权限
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                // 权限获取失败，继续尝试使用
            }
            viewModel.setOutputDirectory(uri)
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val topBarInsets = if (windowWidthSizeClass == WindowWidthSizeClass.COMPACT) {
        WindowInsets.statusBars
    } else {
        WindowInsets(0, 0, 0, 0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                windowInsets = topBarInsets,
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(innerPadding)
            .padding(innerPadding)
            .padding(16.dp)

        if (windowWidthSizeClass != WindowWidthSizeClass.COMPACT) {
            // 横屏/平板布局：双列，支持滚动
            Row(
                modifier = contentModifier.verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    ThemeSettingsCard(
                        uiState = uiState,
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutputDirectoryCard(
                        uiState = uiState,
                        onSelectDirectory = { directoryPicker.launch(null) },
                        onClearDirectory = { viewModel.clearOutputDirectory() },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                LanguageSettingsCard(
                    uiState = uiState,
                    onSetLanguage = { viewModel.setLanguage(it) },
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            // 竖屏布局：单列可滚动
            Column(
                modifier = contentModifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                ThemeSettingsCard(
                    uiState = uiState,
                    onSetThemeMode = { viewModel.setThemeMode(it) },
                    modifier = Modifier.fillMaxWidth(),
                )
                LanguageSettingsCard(
                    uiState = uiState,
                    onSetLanguage = { viewModel.setLanguage(it) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutputDirectoryCard(
                    uiState = uiState,
                    onSelectDirectory = { directoryPicker.launch(null) },
                    onClearDirectory = { viewModel.clearOutputDirectory() },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ThemeSettingsCard(
    uiState: SettingsUiState,
    onSetThemeMode: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_theme_title),
                style = MaterialTheme.typography.titleMedium,
            )

            val themeOptions = listOf(
                "system" to stringResource(R.string.theme_system),
                "light" to stringResource(R.string.theme_light),
                "dark" to stringResource(R.string.theme_dark),
            )

            themeOptions.forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = uiState.themeMode == value,
                            onClick = { onSetThemeMode(value) },
                            role = Role.RadioButton,
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = uiState.themeMode == value,
                        onClick = null,
                    )
                    Text(
                        text = label,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageSettingsCard(
    uiState: SettingsUiState,
    onSetLanguage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_language_title),
                style = MaterialTheme.typography.titleMedium,
            )

            val languageOptions = listOf(
                "system" to stringResource(R.string.language_system),
                "zh" to stringResource(R.string.language_chinese),
                "en" to stringResource(R.string.language_english),
            )

            languageOptions.forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = uiState.language == value,
                            onClick = { onSetLanguage(value) },
                            role = Role.RadioButton,
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = uiState.language == value,
                        onClick = null,
                    )
                    Text(
                        text = label,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun OutputDirectoryCard(
    uiState: SettingsUiState,
    onSelectDirectory: () -> Unit,
    onClearDirectory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_output_directory_title),
                style = MaterialTheme.typography.titleMedium,
            )

            if (uiState.hasOutputDirectory) {
                // 已选择目录
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_output_directory_current),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = uiState.getReadableOutputDirectory(context) ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = onSelectDirectory,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.settings_output_directory_change))
                        }
                        OutlinedButton(
                            onClick = onClearDirectory,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.settings_output_directory_clear))
                        }
                    }
                }
            } else {
                // 未选择目录
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_output_directory_not_set),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(
                        onClick = onSelectDirectory,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_output_directory_select))
                    }
                }
            }
        }
    }
}

package com.tpgszhq.jh.screens.settings.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tpgszhq.jh.R
import com.tpgszhq.jh.data.settings.ThemeMode

/** 页面专用弹窗：复用 [SingleChoiceDialog]，仅提供主题选项与本地化文案 */
@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
) {
    SingleChoiceDialog(
        title = stringResource(R.string.settings_theme_dialog_title),
        options = ThemeMode.entries,
        selectedOption = currentTheme,
        optionLabel = { themeMode ->
            stringResource(
                when (themeMode) {
                    ThemeMode.SYSTEM -> R.string.theme_system
                    ThemeMode.DARK -> R.string.theme_dark
                    ThemeMode.LIGHT -> R.string.theme_light
                }
            )
        },
        onOptionSelected = onThemeSelected,
        onDismiss = onDismiss,
    )
}

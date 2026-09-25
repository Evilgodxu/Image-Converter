package com.tpgszhq.jh.screens.settings.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tpgszhq.jh.R
import com.tpgszhq.jh.data.settings.AppLanguage

/** 页面专用弹窗：复用 [SingleChoiceDialog]，仅提供语言选项与本地化文案 */
@Composable
fun LanguageSelectionDialog(
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    SingleChoiceDialog(
        title = stringResource(R.string.settings_language_dialog_title),
        options = AppLanguage.entries,
        selectedOption = currentLanguage,
        optionLabel = { language ->
            stringResource(
                when (language) {
                    AppLanguage.SYSTEM -> R.string.language_system
                    AppLanguage.CHINESE -> R.string.language_chinese
                    AppLanguage.ENGLISH -> R.string.language_english
                }
            )
        },
        onOptionSelected = onLanguageSelected,
        onDismiss = onDismiss,
    )
}

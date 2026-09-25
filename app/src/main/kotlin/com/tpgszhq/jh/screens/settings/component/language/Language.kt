package com.tpgszhq.jh.screens.settings.component.language

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tpgszhq.jh.R
import com.tpgszhq.jh.data.settings.AppLanguage
import com.tpgszhq.jh.screens.settings.component.SettingsClickableItem
import com.tpgszhq.jh.ui.component.SectionCard
import com.tpgszhq.jh.ui.icons.AppIcons

// 语言设置项：展示当前语言并可点击切换
@Composable
fun Language(language: AppLanguage, onShowDialog: () -> Unit) {
    SectionCard(title = stringResource(R.string.settings_section_language)) {
        SettingsClickableItem(
            icon = AppIcons.Language,
            title = stringResource(R.string.settings_language_title),
            subtitle = when (language) {
                AppLanguage.SYSTEM -> stringResource(R.string.language_system)
                AppLanguage.CHINESE -> stringResource(R.string.language_chinese)
                AppLanguage.ENGLISH -> stringResource(R.string.language_english)
            },
            onClick = onShowDialog,
        )
    }
}

package com.tpgszhq.jh.screens.settings.component.appearance

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import com.tpgszhq.jh.R
import com.tpgszhq.jh.data.settings.ThemeMode
import com.tpgszhq.jh.screens.settings.component.SettingsClickableItem
import com.tpgszhq.jh.ui.component.SectionCard
import com.tpgszhq.jh.ui.icons.AppIcons

// 外观设置项：展示当前主题并可点击切换
@Composable
fun Appearance(themeMode: ThemeMode, onThemeClick: (Offset) -> Unit) {
    SectionCard(title = stringResource(R.string.settings_section_appearance)) {
        SettingsClickableItem(
            icon = AppIcons.Palette,
            title = stringResource(R.string.settings_theme_title),
            subtitle = when (themeMode) {
                ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                ThemeMode.DARK -> stringResource(R.string.theme_dark)
                ThemeMode.LIGHT -> stringResource(R.string.theme_light)
            },
            onClick = {},
            onClickWithPosition = { position -> onThemeClick(position) },
        )
    }
}

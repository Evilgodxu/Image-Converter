package com.tpgszhq.jh.screens.settings

import androidx.compose.ui.geometry.Offset

// 设置页 UI 状态：页面级弹窗显隐、主题切换动效起点与输出目录
data class SettingsUiState(
    val showThemeDialog: Boolean = false,
    val showLanguageDialog: Boolean = false,
    val pendingThemeClickPosition: Offset = Offset.Zero,
    val outputDirectory: String? = null,
) {
    val hasOutputDirectory: Boolean = outputDirectory != null
}

package com.tpgszhq.jh

import com.tpgszhq.jh.data.settings.AppLanguage
import com.tpgszhq.jh.data.settings.ThemeMode

// 应用级 UI 状态：聚合全局主题、语言、版本与隐私同意状态，供主题、本地化与各页面 UI 共同消费
data class AppUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val version: String = "",
    // null 表示尚未从 DataStore 读出，用于避免冷启动时先闪隐私页再跳首页
    val privacyAccepted: Boolean? = null,
)

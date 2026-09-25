package com.tpgszhq.jh.data.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SettingsKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    // 语言统一落 DataStore，由 Compose 层驱动热切换
    val LANGUAGE = stringPreferencesKey("language")
    val PRIVACY_ACCEPTED = booleanPreferencesKey("privacy_accepted")
    // 图片转换输出目录（SAF 树 URI）
    val OUTPUT_DIRECTORY = stringPreferencesKey("output_directory")
}

// 应用主题模式
enum class ThemeMode(val value: String) {
    SYSTEM("system"),
    DARK("dark"),
    LIGHT("light");

    companion object {
        fun fromValue(value: String): ThemeMode = entries.find { it.value == value } ?: SYSTEM
    }
}

// 应用语言：中文/英文/跟随系统
enum class AppLanguage(val languageTag: String?) {
    SYSTEM(null),
    CHINESE("zh"),
    ENGLISH("en");
}

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

package com.tpgszhq.jh.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.tpgszhq.jh.data.settings.AppLanguage
import com.tpgszhq.jh.data.settings.SettingsKeys
import com.tpgszhq.jh.data.settings.SettingsState
import com.tpgszhq.jh.data.settings.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 设置仓库契约：数据层入口，AppContainer 注入实现，测试注入 fake
interface SettingsRepository {
    // 设置状态流：主题模式驱动全局配色
    val settings: Flow<SettingsState>

    // 应用语言流：统一读 DataStore
    val appLanguage: Flow<AppLanguage>

    // 隐私政策同意状态：决定冷启动落地页
    val privacyAccepted: Flow<Boolean>

    // 输出目录流：转换输出与设置页展示共用
    val outputDirectory: Flow<String?>

    suspend fun saveThemeMode(mode: ThemeMode)

    // 设置应用语言：统一写入 DataStore，由 Compose 层驱动热切换
    suspend fun setAppLanguage(language: AppLanguage)

    suspend fun setPrivacyAccepted(accepted: Boolean)

    suspend fun setOutputDirectory(uri: String?)
}

// DataStore 实现：以 DataStore 为单一事实源，数据源经构造注入便于单测
class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<SettingsState> = dataStore.data.map { preferences ->
        SettingsState(
            themeMode = ThemeMode.fromValue(preferences[SettingsKeys.THEME_MODE] ?: ThemeMode.SYSTEM.value),
        )
    }

    override val appLanguage: Flow<AppLanguage> = dataStore.data.map { preferences ->
        AppLanguage.entries.find { it.languageTag == preferences[SettingsKeys.LANGUAGE] } ?: AppLanguage.SYSTEM
    }

    override val privacyAccepted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SettingsKeys.PRIVACY_ACCEPTED] ?: false
    }

    override val outputDirectory: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SettingsKeys.OUTPUT_DIRECTORY]
    }

    override suspend fun saveThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.THEME_MODE] = mode.value
        }
    }

    override suspend fun setAppLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.LANGUAGE] = language.languageTag.orEmpty()
        }
    }

    override suspend fun setPrivacyAccepted(accepted: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.PRIVACY_ACCEPTED] = accepted
        }
    }

    override suspend fun setOutputDirectory(uri: String?) {
        dataStore.edit { preferences ->
            if (uri != null) {
                preferences[SettingsKeys.OUTPUT_DIRECTORY] = uri
            } else {
                preferences.remove(SettingsKeys.OUTPUT_DIRECTORY)
            }
        }
    }
}

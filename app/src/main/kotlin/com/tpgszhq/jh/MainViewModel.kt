package com.tpgszhq.jh

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tpgszhq.jh.data.repository.SettingsRepository
import com.tpgszhq.jh.data.settings.AppLanguage
import com.tpgszhq.jh.data.settings.ThemeMode
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 应用级（Activity 作用域）UI 状态持有者：以 DataStore 为单一事实源，聚合主题、语言、版本与隐私同意状态，
// 供全局主题、本地化与各页面 UI 共同消费，UI 层不直连数据源。
class MainViewModel(
    private val settingsRepository: SettingsRepository,
    appVersion: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState(version = appVersion))
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        // 数据流回流更新 UI 状态，写入后经同一持有者广播
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.update { it.copy(themeMode = settings.themeMode) }
            }
        }
        viewModelScope.launch {
            settingsRepository.appLanguage.collect { language ->
                _uiState.update { it.copy(language = language) }
            }
        }
        viewModelScope.launch {
            settingsRepository.privacyAccepted.collect { accepted ->
                _uiState.update { it.copy(privacyAccepted = accepted) }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.saveThemeMode(mode) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { settingsRepository.setAppLanguage(language) }
    }

    fun setPrivacyAccepted(accepted: Boolean) {
        viewModelScope.launch { settingsRepository.setPrivacyAccepted(accepted) }
    }

    // 退出应用：先清理缓存目录与图片缓存，再结束进程。
    // 临时图片、下载中的更新包等均落在 cacheDir 下，一次递归清理即可覆盖
    fun clearCacheAndExit(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                listOf(context.cacheDir, context.codeCacheDir).forEach { dir ->
                    dir.listFiles()?.forEach { deleteRecursively(it) }
                }
                coil3.SingletonImageLoader.get(context).diskCache?.clear()
                coil3.SingletonImageLoader.get(context).memoryCache?.clear()
            } catch (e: Exception) {
                // 清理失败也继续退出
            } finally {
                withContext(Dispatchers.Main) {
                    android.os.Process.killProcess(android.os.Process.myPid())
                }
            }
        }
    }

    private fun deleteRecursively(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteRecursively(it) }
        }
        file.delete()
    }
}

// 供界面树消费的 CompositionLocal，由宿主 Activity 提供
val LocalMainViewModel = staticCompositionLocalOf<MainViewModel> {
    error("MainViewModel is not provided")
}

package com.tpgszhq.jh.screens.settings

import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tpgszhq.jh.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 设置页 ViewModel：仅持有页面级状态（弹窗显隐、动效起点、输出目录）；
// 主题、语言、版本与更新检查均属应用级，分别由 MainViewModel、AppUpdateViewModel 提供
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.outputDirectory.collect { directory ->
                _uiState.update { it.copy(outputDirectory = directory) }
            }
        }
    }

    // 点击主题项：记录动效起点并打开主题弹窗
    fun showThemeDialog(position: Offset) {
        _uiState.update { it.copy(showThemeDialog = true, pendingThemeClickPosition = position) }
    }

    fun dismissThemeDialog() {
        _uiState.update { it.copy(showThemeDialog = false) }
    }

    fun showLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = true) }
    }

    fun dismissLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = false) }
    }

    fun setOutputDirectory(uri: Uri) {
        viewModelScope.launch { settingsRepository.setOutputDirectory(uri.toString()) }
    }

    fun clearOutputDirectory() {
        viewModelScope.launch { settingsRepository.setOutputDirectory(null) }
    }
}

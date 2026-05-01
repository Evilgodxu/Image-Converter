package com.tpgszhq.jh.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tpgszhq.jh.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.themeMode,
        userPreferencesRepository.language,
        userPreferencesRepository.outputDirectory,
    ) { themeMode, language, outputDirectory ->
        SettingsUiState(
            isLoading = false,
            themeMode = themeMode,
            language = language,
            outputDirectory = outputDirectory,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(isLoading = true),
    )

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            userPreferencesRepository.setLanguage(language)
        }
    }

    fun setOutputDirectory(uri: Uri?) {
        viewModelScope.launch {
            userPreferencesRepository.setOutputDirectory(uri?.toString())
        }
    }

    fun clearOutputDirectory() {
        viewModelScope.launch {
            userPreferencesRepository.setOutputDirectory(null)
        }
    }
}

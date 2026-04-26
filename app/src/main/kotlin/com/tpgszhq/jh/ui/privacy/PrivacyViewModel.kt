package com.tpgszhq.jh.ui.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tpgszhq.jh.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrivacyViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PrivacyUiState())
    val uiState: StateFlow<PrivacyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val accepted = userPreferencesRepository.privacyAccepted.first()
            _uiState.value = PrivacyUiState(
                isLoading = false,
                privacyAccepted = accepted,
            )
        }
    }

    fun acceptPrivacy(onAccepted: () -> Unit) {
        viewModelScope.launch {
            userPreferencesRepository.setPrivacyAccepted(true)
            onAccepted()
        }
    }

    fun rejectPrivacy(onRejected: () -> Unit) {
        onRejected()
    }
}

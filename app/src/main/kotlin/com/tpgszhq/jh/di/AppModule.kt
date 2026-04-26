package com.tpgszhq.jh.di

import com.tpgszhq.jh.data.repository.UserPreferencesRepository
import com.tpgszhq.jh.ui.home.HomeViewModel
import com.tpgszhq.jh.ui.privacy.PrivacyViewModel
import com.tpgszhq.jh.ui.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { UserPreferencesRepository(androidContext()) }
    viewModel { PrivacyViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
}

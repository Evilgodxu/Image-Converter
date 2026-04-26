package com.tpgszhq.jh.ui.localization

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tpgszhq.jh.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

class LanguageManager(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    val languageFlow: Flow<String> = userPreferencesRepository.language

    val localeFlow: Flow<Locale> = languageFlow.map { languageCode ->
        resolveLocale(languageCode)
    }

    fun resolveLocale(languageCode: String): Locale {
        return when (languageCode) {
            "zh" -> Locale.SIMPLIFIED_CHINESE
            "en" -> Locale.ENGLISH
            else -> Locale.getDefault()
        }
    }

    fun getLocalizedResources(locale: Locale): Resources {
        val config = Configuration()
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)
        return localizedContext.resources
    }
}

val LocalLanguageManager = compositionLocalOf<LanguageManager> {
    error("LanguageManager not provided")
}

@Composable
fun ProvideLanguageManager(languageManager: LanguageManager, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLanguageManager provides languageManager) {
        content()
    }
}

@Composable
fun stringResource(id: Int): String {
    val languageManager = LocalLanguageManager.current
    val locale by languageManager.localeFlow.collectAsStateWithLifecycle(
        initialValue = LocalLocale.current.platformLocale
    )
    val resources = languageManager.getLocalizedResources(locale)
    return resources.getString(id)
}

@Composable
fun stringResource(id: Int, vararg formatArgs: Any): String {
    val languageManager = LocalLanguageManager.current
    val locale by languageManager.localeFlow.collectAsStateWithLifecycle(
        initialValue = LocalLocale.current.platformLocale
    )
    val resources = languageManager.getLocalizedResources(locale)
    return resources.getString(id, *formatArgs)
}

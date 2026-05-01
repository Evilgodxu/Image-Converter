package com.tpgszhq.jh.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tpgszhq.jh.ui.home.HomeScreen
import com.tpgszhq.jh.ui.privacy.PrivacyScreen
import com.tpgszhq.jh.ui.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    onExitApp: () -> Unit,
    externalImageUris: List<Uri> = emptyList(),
    language: String = "system",
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(Screen.Privacy.route) {
            PrivacyScreen(
                onPrivacyAccepted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Privacy.route) { inclusive = true }
                    }
                },
                onPrivacyRejected = onExitApp,
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                externalImageUris = externalImageUris,
                language = language,
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
            )
        }
    }
}

package com.tpgszhq.jh.ui.navigation

sealed class Screen(val route: String) {
    data object Privacy : Screen("privacy")
    data object Home : Screen("home")
    data object Settings : Screen("settings")
}

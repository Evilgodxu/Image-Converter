package com.tpgszhq.jh.ui.adaptive

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.window.core.layout.WindowWidthSizeClass

val LocalWindowWidthSizeClass = compositionLocalOf<WindowWidthSizeClass> {
    error("WindowWidthSizeClass not provided")
}

@Composable
fun ProvideWindowSizeClass(content: @Composable () -> Unit) {
    val windowWidthSizeClass = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass
    CompositionLocalProvider(LocalWindowWidthSizeClass provides windowWidthSizeClass) {
        content()
    }
}

@Composable
fun rememberWindowWidthSizeClass(): WindowWidthSizeClass {
    return LocalWindowWidthSizeClass.current
}

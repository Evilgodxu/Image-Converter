package com.tpgszhq.jh.ui.adaptive

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.window.core.layout.WindowWidthSizeClass

enum class DeviceType {
    COMPACT, // 手机竖屏
    MEDIUM, // 手机横屏/小平板
    EXPANDED, // 平板/大屏设备
}

class WindowSizeInfo(
    val widthSizeClass: WindowWidthSizeClass,
    val deviceType: DeviceType,
) {
    val isCompact: Boolean
        get() = deviceType == DeviceType.COMPACT

    val isMedium: Boolean
        get() = deviceType == DeviceType.MEDIUM

    val isExpanded: Boolean
        get() = deviceType == DeviceType.EXPANDED

    val isTablet: Boolean
        get() = deviceType == DeviceType.MEDIUM || deviceType == DeviceType.EXPANDED
}

val LocalWindowSizeInfo = compositionLocalOf<WindowSizeInfo> {
    error("WindowSizeInfo not provided")
}

@Composable
fun ProvideWindowSizeInfo(content: @Composable () -> Unit) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val windowSizeClass = adaptiveInfo.windowSizeClass.windowWidthSizeClass

    val deviceType = when (windowSizeClass) {
        WindowWidthSizeClass.COMPACT -> DeviceType.COMPACT
        WindowWidthSizeClass.MEDIUM -> DeviceType.MEDIUM
        WindowWidthSizeClass.EXPANDED -> DeviceType.EXPANDED
        else -> DeviceType.COMPACT
    }

    val windowSizeInfo = WindowSizeInfo(
        widthSizeClass = windowSizeClass,
        deviceType = deviceType,
    )

    CompositionLocalProvider(LocalWindowSizeInfo provides windowSizeInfo) {
        content()
    }
}

@Composable
fun rememberWindowSizeInfo(): WindowSizeInfo {
    return LocalWindowSizeInfo.current
}

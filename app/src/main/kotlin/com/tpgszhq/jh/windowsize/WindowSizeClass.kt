package com.tpgszhq.jh.windowsize

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

// 窗口宽度尺寸类：compact <600dp、medium 600-839dp、expanded >=840dp
enum class WindowSizeClass {
    Compact,
    Medium,
    Expanded,
}

// 读取当前窗口宽度尺寸类；窗口尺寸变化时自动重组
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    // 取窗口容器尺寸而非 Configuration.screenWidthDp：后者与 inset 行为耦合且被四舍五入，
    // 不能准确反映实际可用窗口宽度
    val density = LocalDensity.current
    val containerWidth = with(density) { LocalWindowInfo.current.containerSize.width.toDp() }
    return when {
        containerWidth >= 840.dp -> WindowSizeClass.Expanded
        containerWidth >= 600.dp -> WindowSizeClass.Medium
        else -> WindowSizeClass.Compact
    }
}

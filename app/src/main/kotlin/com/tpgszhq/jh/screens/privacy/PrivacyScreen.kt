package com.tpgszhq.jh.screens.privacy

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tpgszhq.jh.LocalMainViewModel
import com.tpgszhq.jh.screens.privacy.compact.PrivacyCompactAssembly
import com.tpgszhq.jh.screens.privacy.expanded.PrivacyExpandedAssembly
import com.tpgszhq.jh.windowsize.WindowSizeClass
import com.tpgszhq.jh.windowsize.rememberWindowSizeClass

// 页面入口：装配隐私说明与同意/拒绝事件，按窗口尺寸类分发形态，不含布局。
// 首启未同意时作为起始页，拒绝即退出，同意后进入首页并持久化
@Composable
fun PrivacyScreen(
    onAccepted: () -> Unit,
    onRejected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mainViewModel = LocalMainViewModel.current

    // 先落盘同意状态再放行，保证下次冷启动直接进入首页
    val onAccept: () -> Unit = {
        mainViewModel.setPrivacyAccepted(true)
        onAccepted()
    }

    when (rememberWindowSizeClass()) {
        WindowSizeClass.Compact -> PrivacyCompactAssembly(
            onAccept = onAccept,
            onReject = onRejected,
            modifier = modifier,
        )

        WindowSizeClass.Medium, WindowSizeClass.Expanded -> PrivacyExpandedAssembly(
            onAccept = onAccept,
            onReject = onRejected,
            modifier = modifier,
        )
    }
}

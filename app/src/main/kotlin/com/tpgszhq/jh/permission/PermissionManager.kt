package com.tpgszhq.jh.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

// 权限状态管理器：统一收口运行时权限申请，避免各页面各自散写 launcher
class PermissionManager(
    private val context: Context,
    private val launchRequest: (permission: String, onResult: (Boolean) -> Unit) -> Unit,
) {

    fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    // 已授权立即回调，否则发起系统权限申请
    fun request(permission: String, onResult: (Boolean) -> Unit = {}) {
        if (isGranted(permission)) {
            onResult(true)
        } else {
            launchRequest(permission, onResult)
        }
    }
}

// 创建绑定当前上下文的权限状态管理器
@Composable
fun rememberPermissionManager(): PermissionManager {
    val context = LocalContext.current
    val pending = remember { PendingPermissionResult() }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        pending.consume()?.invoke(granted)
    }
    return remember(context, launcher) {
        PermissionManager(context) { permission, onResult ->
            pending.callback = onResult
            launcher.launch(permission)
        }
    }
}

// 暂存申请回调，等待系统结果返回后消费一次
private class PendingPermissionResult {
    var callback: ((Boolean) -> Unit)? = null

    fun consume(): ((Boolean) -> Unit)? = callback.also { callback = null }
}

package com.tpgszhq.jh.ui.component.dialog

import android.Manifest
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tpgszhq.jh.R
import com.tpgszhq.jh.permission.rememberPermissionManager
import com.tpgszhq.jh.update.UpdateDownloadFailure
import com.tpgszhq.jh.update.UpdateInfo
import com.tpgszhq.jh.update.UpdatePhase
import com.tpgszhq.jh.update.UpdateUiState

// 更新内容区最大高度，避免内容过长把对话框撑满屏幕
private val ChangelogMaxHeight = 300.dp

// 更新对话框：发现新版本 / 下载中 / 下载失败 / 待安装 四种形态；
// 点击对话框外部只关闭对话框，不影响后台下载
@Composable
fun UpdateDialog(
    state: UpdateUiState,
    onDismiss: () -> Unit,
    onStartDownload: () -> Unit,
    onRetry: () -> Unit,
    onInstall: () -> Unit,
) {
    when (val phase = state.phase) {
        UpdatePhase.Idle -> Unit
        is UpdatePhase.Available -> AvailableDialog(phase.info, onDismiss, onStartDownload)
        is UpdatePhase.Downloading -> DownloadingDialog(phase.percent, onDismiss)
        is UpdatePhase.DownloadFailed -> DownloadFailedDialog(phase.failure, onDismiss, onRetry)
        UpdatePhase.InstallReady -> InstallReadyDialog(onDismiss, onInstall)
    }
}

// 发现新版本：展示更新内容与"稍后 / 更新"
@Composable
private fun AvailableDialog(info: UpdateInfo, onDismiss: () -> Unit, onStartDownload: () -> Unit) {
    val permissionManager = rememberPermissionManager()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.update_dialog_title, info.version)) },
        text = {
            Text(
                text = info.changelog.ifBlank { stringResource(R.string.update_dialog_no_changelog) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = ChangelogMaxHeight)
                    .verticalScroll(rememberScrollState()),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // 下载通知需要通知权限；被拒时下载仍可继续，仅通知不可见
                    permissionManager.request(Manifest.permission.POST_NOTIFICATIONS) { onStartDownload() }
                },
            ) {
                Text(stringResource(R.string.update_action_update))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.update_action_later)) }
        },
    )
}

// 下载中：进度条 + 百分比，"隐藏"只关对话框
@Composable
private fun DownloadingDialog(percent: Int, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.update_dialog_downloading_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { percent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.update_dialog_progress, percent),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.update_action_hide)) }
        },
    )
}

// 下载失败：展示失败原因与"稍后 / 重试"
@Composable
private fun DownloadFailedDialog(
    failure: UpdateDownloadFailure,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.update_dialog_failed_title)) },
        text = { Text(stringResource(failure.messageRes)) },
        confirmButton = {
            TextButton(onClick = onRetry) { Text(stringResource(R.string.update_action_retry)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.update_action_later)) }
        },
    )
}

// 待安装：由用户确认后交给系统安装器；关闭（稍后/点外部）后，下次回到前台会再次弹出
@Composable
private fun InstallReadyDialog(onDismiss: () -> Unit, onInstall: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.update_dialog_install_ready_title)) },
        text = { Text(stringResource(R.string.update_dialog_install_ready_hint)) },
        confirmButton = {
            TextButton(onClick = onInstall) { Text(stringResource(R.string.update_action_update)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.update_action_later)) }
        },
    )
}

@get:StringRes
private val UpdateDownloadFailure.messageRes: Int
    get() = when (this) {
        UpdateDownloadFailure.Timeout -> R.string.update_failure_timeout
        UpdateDownloadFailure.Network -> R.string.update_failure_network
        UpdateDownloadFailure.DigestMismatch -> R.string.update_failure_digest
        UpdateDownloadFailure.Storage -> R.string.update_failure_storage
    }

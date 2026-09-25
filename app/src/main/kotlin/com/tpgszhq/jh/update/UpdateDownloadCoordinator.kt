package com.tpgszhq.jh.update

import android.content.Context
import androidx.core.content.ContextCompat
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

// 下载状态：前台服务写入，界面层消费
sealed interface UpdateDownloadState {
    data object Idle : UpdateDownloadState

    data class Downloading(val percent: Int) : UpdateDownloadState

    data class Completed(val file: File) : UpdateDownloadState

    data class Failed(val failure: UpdateDownloadFailure) : UpdateDownloadState
}

// 下载协调器（应用级单例）：对外暴露下载状态、启动前台下载服务，并由服务回写进度与结果；
// 待安装包落在应用缓存目录，进程被杀后仍可由磁盘恢复
class UpdateDownloadCoordinator(private val appContext: Context) {

    private val downloadDir: File get() = File(appContext.cacheDir, DOWNLOAD_DIR_NAME)

    private val _state = MutableStateFlow<UpdateDownloadState>(UpdateDownloadState.Idle)
    val state: StateFlow<UpdateDownloadState> = _state.asStateFlow()

    fun start(info: UpdateInfo) {
        _state.value = UpdateDownloadState.Downloading(0)
        ContextCompat.startForegroundService(
            appContext,
            UpdateDownloadService.createIntent(appContext, info),
        )
    }

    // 指定版本的安装包落盘位置
    fun downloadFile(version: String): File = File(downloadDir, pendingApkFileName(version))

    // 指定版本是否已有下载并通过校验的安装包
    suspend fun hasPendingInstall(version: String): Boolean = withContext(Dispatchers.IO) {
        downloadFile(version).isFile
    }

    // 可安装的本地包：优先取内存中的完成态，否则回落到磁盘扫描（进程重启后无完成态）
    suspend fun installableFile(currentVersion: String): File? = withContext(Dispatchers.IO) {
        (_state.value as? UpdateDownloadState.Completed)?.file?.takeIf { it.isFile }
            ?: findPendingInstallOnDisk(currentVersion)
    }

    // 由磁盘恢复待安装态：仅在空闲/失败（即未在下载、也非已完成）时执行，避免打断进行中的状态
    suspend fun restorePendingInstall(currentVersion: String) {
        val current = _state.value
        if (current is UpdateDownloadState.Completed || current is UpdateDownloadState.Downloading) return
        withContext(Dispatchers.IO) { findPendingInstallOnDisk(currentVersion) }
            ?.let { _state.value = UpdateDownloadState.Completed(it) }
    }

    // 以下由 UpdateDownloadService 回调
    fun onProgress(percent: Int) {
        _state.value = UpdateDownloadState.Downloading(percent)
    }

    fun onCompleted(file: File) {
        _state.value = UpdateDownloadState.Completed(file)
    }

    fun onFailed(failure: UpdateDownloadFailure) {
        _state.value = UpdateDownloadState.Failed(failure)
    }

    fun reset() {
        _state.value = UpdateDownloadState.Idle
    }

    // 应用内点击安装后，撤掉服务留下的完成通知
    fun clearNotification() {
        UpdateNotifications.cancel(appContext)
    }

    // 找出待安装包（版本需新于 currentVersion），并清理其余残留（含升级成功后遗留的旧版本包）
    private fun findPendingInstallOnDisk(currentVersion: String): File? {
        val candidates = downloadDir.listFiles { file ->
            file.isFile && pendingApkVersion(file.name) != null
        }.orEmpty()
        val pending = candidates.firstOrNull { file ->
            pendingApkVersion(file.name)?.let { AppUpdateChecker.hasNewVersion(it, currentVersion) } == true
        }
        candidates.filter { it != pending }.forEach { it.delete() }
        return pending
    }

    private companion object {
        const val DOWNLOAD_DIR_NAME = "updates"
    }
}

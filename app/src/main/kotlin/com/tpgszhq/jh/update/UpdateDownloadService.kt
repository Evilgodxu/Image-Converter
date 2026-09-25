package com.tpgszhq.jh.update

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.tpgszhq.jh.App
import com.tpgszhq.jh.data.settings.AppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// 前台下载服务：应用退到后台仍继续下载，并在通知栏展示进度；
// 完成后交回下载协调器，由界面层决定何时安装
class UpdateDownloadService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var downloadJob: Job? = null

    private val container get() = (application as App).container

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val info = intent?.toUpdateInfo()
        if (info == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        downloadJob?.cancel()
        downloadJob = serviceScope.launch { runDownload(info) }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private suspend fun runDownload(info: UpdateInfo) {
        // 通知文案跟随应用内语言：Service 自身上下文只有系统语言，需另建本地化上下文后再取资源
        val notificationContext = notificationContext()
        UpdateNotifications.createChannel(notificationContext)
        ServiceCompat.startForeground(
            this,
            UpdateNotifications.NOTIFICATION_ID,
            UpdateNotifications.progress(notificationContext, 0),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
        try {
            val targetFile = container.updateDownloadCoordinator.downloadFile(info.version)
            UpdateDownloader.download(info, targetFile) { percent ->
                UpdateNotifications.post(this, UpdateNotifications.progress(notificationContext, percent))
            }
            container.updateDownloadCoordinator.onCompleted(targetFile)
            UpdateNotifications.post(
                this,
                UpdateNotifications.completed(notificationContext, container.apkInstaller.installIntent(targetFile)),
            )
            // 解除前台状态但保留通知，供应用在后台时点击安装
            stopForeground(STOP_FOREGROUND_DETACH)
        } catch (e: UpdateDownloadException) {
            container.updateDownloadCoordinator.onFailed(e.failure)
            UpdateNotifications.post(this, UpdateNotifications.failed(notificationContext))
            stopForeground(STOP_FOREGROUND_DETACH)
        } finally {
            stopSelf()
        }
    }

    // 按应用内语言构造本地化上下文；读取失败时回落到跟随系统
    private suspend fun notificationContext(): Context {
        val currentContainer = container
        val language = runCatching { currentContainer.settingsRepository.appLanguage.first() }
            .getOrNull() ?: AppLanguage.SYSTEM
        val manager = currentContainer.localizationManager
        return manager.createLocalizedContext(manager.localeFor(language))
    }

    companion object {
        private const val EXTRA_VERSION = "extra_version"
        private const val EXTRA_CHANGELOG = "extra_changelog"
        private const val EXTRA_APK_URL = "extra_apk_url"
        private const val EXTRA_APK_SIZE = "extra_apk_size"
        private const val EXTRA_APK_SHA256 = "extra_apk_sha256"

        fun createIntent(context: Context, info: UpdateInfo): Intent =
            Intent(context, UpdateDownloadService::class.java).apply {
                putExtra(EXTRA_VERSION, info.version)
                putExtra(EXTRA_CHANGELOG, info.changelog)
                putExtra(EXTRA_APK_URL, info.apkUrl)
                putExtra(EXTRA_APK_SIZE, info.apkSize)
                putExtra(EXTRA_APK_SHA256, info.apkSha256)
            }

        private fun Intent.toUpdateInfo(): UpdateInfo? {
            val version = getStringExtra(EXTRA_VERSION) ?: return null
            val apkUrl = getStringExtra(EXTRA_APK_URL) ?: return null
            val sha256 = getStringExtra(EXTRA_APK_SHA256) ?: return null
            return UpdateInfo(
                version = version,
                changelog = getStringExtra(EXTRA_CHANGELOG).orEmpty(),
                apkUrl = apkUrl,
                apkSize = getLongExtra(EXTRA_APK_SIZE, 0L),
                apkSha256 = sha256,
            )
        }
    }
}

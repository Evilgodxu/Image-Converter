package com.tpgszhq.jh.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

// 安装器入口：只负责把已校验的安装包交给系统安装器；
// 安装授权由系统安装器自行引导，应用不做权限检查、不跳设置页、不实现安装逻辑
class ApkInstaller(private val appContext: Context) {

    // 系统安装器 Intent：经 FileProvider 暴露缓存目录中的安装包
    fun installIntent(apkFile: File): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            apkFile,
        )
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, APK_MIME_TYPE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun install(apkFile: File) {
        appContext.startActivity(installIntent(apkFile).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private companion object {
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}

package com.tpgszhq.jh

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.tpgszhq.jh.data.repository.DataStoreSettingsRepository
import com.tpgszhq.jh.data.repository.DataStoreUpdateCheckRepository
import com.tpgszhq.jh.data.repository.SettingsRepository
import com.tpgszhq.jh.data.repository.UpdateCheckRepository
import com.tpgszhq.jh.localization.LocalizationManager
import com.tpgszhq.jh.update.ApkInstaller
import com.tpgszhq.jh.update.UpdateDownloadCoordinator

// 手动 DI 容器：Application 启动时构造并持有全部依赖
// 数据源经构造注入仓库，便于单元测试替换
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val settingsRepository: SettingsRepository by lazy {
        DataStoreSettingsRepository(preferencesDataStore)
    }

    val updateCheckRepository: UpdateCheckRepository by lazy {
        DataStoreUpdateCheckRepository(preferencesDataStore)
    }

    val localizationManager: LocalizationManager by lazy {
        LocalizationManager(appContext)
    }

    // 更新下载：协调器持有下载状态并启动前台服务，安装器负责拉起系统安装流程
    val updateDownloadCoordinator: UpdateDownloadCoordinator by lazy {
        UpdateDownloadCoordinator(appContext)
    }

    val apkInstaller: ApkInstaller by lazy {
        ApkInstaller(appContext)
    }

    // 应用版本号：冷启动读取一次
    val appVersion: String = readAppVersion()

    // 沿用既有文件名，避免升级后旧设置（隐私同意、输出目录等）丢失
    private val preferencesDataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create {
            appContext.preferencesDataStoreFile("user_preferences")
        }
    }

    private fun readAppVersion(): String =
        appContext.packageManager
            .getPackageInfo(appContext.packageName, PackageManager.PackageInfoFlags.of(0L))
            .versionName.orEmpty()
}

// 供界面树取依赖的 CompositionLocal，由宿主 Activity 提供，页面级 ViewModel 据此构造
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer is not provided")
}

package com.tpgszhq.jh

import android.app.Application
import com.tpgszhq.jh.log.CrashLogManager

class App : Application() {

    // 手动 DI 容器：Application 级单例，宿主 Activity 启动时取用
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // 最先初始化崩溃日志，捕获启动阶段异常
        CrashLogManager.init(this)
        container = AppContainer(this)
    }
}

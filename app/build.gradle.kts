import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.tpgszhq.jh"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.tpgszhq.jh"
        minSdk = 33
        targetSdk = 37
        versionCode = 6
        versionName = "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("../jh.keystore")
            storePassword = localProperties.getProperty("KEYSTORE_PASSWORD", "")
            keyAlias = localProperties.getProperty("KEY_ALIAS", "jh")
            keyPassword = localProperties.getProperty("KEY_PASSWORD", "")
        }
    }

    androidResources {
        // 禁用按语言分包，支持应用内语言切换
        localeFilters += listOf("zh", "en")
    }

    // 语言资源不分包，确保运行时语言切换可用
    bundle {
        language {
            enableSplit = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isDebuggable = true
        }
    }

    packaging {
        resources {
            excludes += setOf(
                // Kotlin 模块元数据文件（每个 Kotlin 库都会生成，必然重复）
                "META-INF/*.kotlin_module",
                // Kotlin 协程调试探针
                "META-INF/DebugProbesKt.bin",
                "DebugProbesKt.bin",
                // 常见的重复许可证文件
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/LICENSE.md",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/NOTICE.md",
                // 版本控制索引文件
                "META-INF/INDEX.LIST",
                // 第三方库签名文件
                "META-INF/*.SF",
                "META-INF/*.DSA",
                "META-INF/*.RSA",
                // AndroidX 版本属性文件（各库独立包含，无需打包）
                "META-INF/*.version",
                "META-INF/androidx/**",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
    lint {
        // 质量门禁：Lint Error 阻断构建，release 变体同规则
        abortOnError = true
        checkReleaseBuilds = true
    }
}

// 构建产物统一命名为 ImageConverter-<versionName>-arm64.apk
val apkVersionName = android.defaultConfig.versionName ?: "0.0.0"

kotlin {
    compilerOptions {
        // Kotlin 编译警告提级为 Error，弃用 API 告警源头清零
        allWarningsAsErrors.set(true)
    }
}

androidComponents {
    onVariants(selector().all()) { variant ->
        variant.outputs.forEach { output ->
            output.outputFileName.set("ImageConverter-$apkVersionName-arm64.apk")
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // 导航框架 Navigation3
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    // Navigation3 页面级 ViewModel 作用域支持
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // 键值存储 DataStore
    implementation(libs.androidx.datastore.preferences)

    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)

    // 图片加载：首页图片预览
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

pluginManagement {
    // 依赖版本巡检插件改用本地维护分支构建，源码位于同级目录
    includeBuild("../refreshVersions/plugins")
    repositories {
        // 插件与依赖优先走腾讯云镜像，Google 与 AndroidX 包由 google() 兜底
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/gradle-plugins/") }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}
plugins {
    // Gradle 8.8+ 已内置 Foojay Toolchain Resolver，无需额外声明
    id("de.fayard.refreshVersions")
}

refreshVersions {
    // 只接受正式版本：预发布标签与 JetBrains IDE 内部构建号都带连字符，
    // 而插件对无法识别的后缀会兜底判为 Stable，故按连字符特征统一滤除
    rejectVersionIf {
        candidate.value.contains('-')
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        google()
        mavenCentral()
    }
}

rootProject.name = "ImageConverter"
include(":app")

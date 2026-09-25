package com.tpgszhq.jh.update

// 线上可用的更新包信息：版本号、更新内容、下载地址与 GitHub 提供的 SHA-256
data class UpdateInfo(
    val version: String,
    val changelog: String,
    val apkUrl: String,
    val apkSize: Long,
    val apkSha256: String,
)

// 待安装包命名约定：update-<版本>.apk，按版本命名便于判断本地是否已下载该版本
private const val PENDING_APK_PREFIX = "update-"
private const val PENDING_APK_SUFFIX = ".apk"
private val UNSAFE_FILE_NAME_CHARS = Regex("[^A-Za-z0-9._-]")

fun pendingApkFileName(version: String): String =
    "$PENDING_APK_PREFIX${version.replace(UNSAFE_FILE_NAME_CHARS, "_")}$PENDING_APK_SUFFIX"

// 由待安装包文件名还原版本；不符合命名约定（如未完成的 .part 文件）时返回 null
fun pendingApkVersion(fileName: String): String? = fileName
    .takeIf { it.startsWith(PENDING_APK_PREFIX) && it.endsWith(PENDING_APK_SUFFIX) }
    ?.substring(PENDING_APK_PREFIX.length, fileName.length - PENDING_APK_SUFFIX.length)
    ?.takeIf { it.isNotBlank() }

// 检查结果：已是最新 / 有新版本 / 检查失败 / 版本更新但缺少可用安装包
sealed interface UpdateCheckResult {
    data object UpToDate : UpdateCheckResult

    data class Available(val info: UpdateInfo) : UpdateCheckResult

    data object CheckFailed : UpdateCheckResult

    data object IncompleteRelease : UpdateCheckResult
}

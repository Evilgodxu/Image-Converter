package com.tpgszhq.jh.update

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 版本检查：以 GitHub Releases 最新版为线上版本，解析更新正文与安装包元数据。
 * 仅做查询与语义化版本比较，不承担下载与安装。
 */
object AppUpdateChecker {

    /** GitHub Releases 最新版查询接口 */
    private const val LATEST_RELEASE_URL = "https://api.github.com/repos/Evilgodxu/Image-Converter/releases/latest"

    private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    private const val SHA256_PREFIX = "sha256:"

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class GitHubRelease(
        @SerialName("tag_name") val tagName: String? = null,
        @SerialName("body") val body: String? = null,
        @SerialName("assets") val assets: List<GitHubAsset> = emptyList(),
    )

    @Serializable
    private data class GitHubAsset(
        @SerialName("content_type") val contentType: String? = null,
        @SerialName("size") val size: Long = 0L,
        @SerialName("digest") val digest: String? = null,
        @SerialName("browser_download_url") val downloadUrl: String? = null,
    )

    /**
     * 查询线上最新版本及其安装包信息。
     * @WorkerThread 内部切至 IO 线程。
     */
    suspend fun fetch(currentVersion: String): UpdateCheckResult = withContext(Dispatchers.IO) {
        val release = runCatching { requestLatestRelease() }.getOrNull()
            ?: return@withContext UpdateCheckResult.CheckFailed
        val latest = release.tagName?.takeIf { it.isNotBlank() }
            ?: return@withContext UpdateCheckResult.CheckFailed
        if (!hasNewVersion(latest, currentVersion)) return@withContext UpdateCheckResult.UpToDate

        // 版本更新但缺少可下载的安装包或摘要时无法安全安装，视为不可用更新
        val apk = release.assets.firstOrNull { it.contentType == APK_MIME_TYPE }
        val apkUrl = apk?.downloadUrl?.takeIf { it.isNotBlank() }
        val sha256 = apk?.digest
            ?.takeIf { it.startsWith(SHA256_PREFIX) }
            ?.removePrefix(SHA256_PREFIX)
            ?.takeIf { it.isNotBlank() }
        if (apkUrl == null || sha256 == null) return@withContext UpdateCheckResult.IncompleteRelease

        UpdateCheckResult.Available(
            UpdateInfo(
                version = latest,
                changelog = release.body.orEmpty().trim(),
                apkUrl = apkUrl,
                apkSize = apk.size,
                apkSha256 = sha256.lowercase(),
            ),
        )
    }

    /** latest 是否比 current 新；忽略前缀 v，缺省位按 0 补齐比较 */
    fun hasNewVersion(latest: String, current: String): Boolean = compareVersions(latest, current) > 0

    private fun requestLatestRelease(): GitHubRelease {
        val connection = URL(LATEST_RELEASE_URL).openConnection() as HttpURLConnection
        connection.apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            setRequestProperty("Accept", "application/vnd.github+json")
        }
        try {
            check(connection.responseCode == HttpURLConnection.HTTP_OK) {
                "unexpected response code ${connection.responseCode}"
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            return json.decodeFromString<GitHubRelease>(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun compareVersions(a: String, b: String): Int {
        val aParts = a.trimStart('v').split('.').map { it.toIntOrNull() ?: 0 }
        val bParts = b.trimStart('v').split('.').map { it.toIntOrNull() ?: 0 }
        val maxLength = maxOf(aParts.size, bParts.size)
        for (index in 0 until maxLength) {
            val diff = aParts.getOrElse(index) { 0 } - bParts.getOrElse(index) { 0 }
            if (diff != 0) return diff
        }
        return 0
    }

    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 10_000
}

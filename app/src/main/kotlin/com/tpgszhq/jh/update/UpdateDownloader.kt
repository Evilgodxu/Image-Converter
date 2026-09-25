package com.tpgszhq.jh.update

import java.io.File
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

// 下载失败原因：超时、网络中断、摘要不匹配、写入失败
enum class UpdateDownloadFailure {
    Timeout,
    Network,
    DigestMismatch,
    Storage,
}

// 下载异常：携带失败原因供界面反馈
class UpdateDownloadException(val failure: UpdateDownloadFailure, cause: Throwable? = null) :
    Exception(failure.name, cause)

// 安装包下载器：流式下载 + 进度回调 + 连接/读取超时 + SHA-256 校验
object UpdateDownloader {

    /**
     * 下载并校验安装包，成功返回本地文件，失败抛 [UpdateDownloadException]。
     * 先写入 .part 临时文件，校验通过后再改名，避免留下不完整文件。
     * @param onProgress 下载进度百分比（0-100）
     */
    suspend fun download(info: UpdateInfo, targetFile: File, onProgress: (Int) -> Unit): File =
        withContext(Dispatchers.IO) {
            val tempFile = File(targetFile.parentFile, "${targetFile.name}.part")
            try {
                val digest = MessageDigest.getInstance(SHA256_ALGORITHM)
                downloadTo(info, tempFile, digest, onProgress)
                verifyDigest(digest, info.apkSha256)
                if (targetFile.exists() && !targetFile.delete()) {
                    throw UpdateDownloadException(UpdateDownloadFailure.Storage)
                }
                if (!tempFile.renameTo(targetFile)) {
                    throw UpdateDownloadException(UpdateDownloadFailure.Storage)
                }
                targetFile
            } catch (e: UpdateDownloadException) {
                tempFile.delete()
                throw e
            } catch (e: Exception) {
                tempFile.delete()
                throw UpdateDownloadException(e.toFailure(), e)
            }
        }

    private suspend fun downloadTo(
        info: UpdateInfo,
        tempFile: File,
        digest: MessageDigest,
        onProgress: (Int) -> Unit,
    ) {
        val connection = URL(info.apkUrl).openConnection() as HttpURLConnection
        connection.apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
        }
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw UpdateDownloadException(UpdateDownloadFailure.Network)
            }
            // 进度以 release 声明的体积为准，缺失时退化为响应头长度
            val totalBytes = info.apkSize.takeIf { it > 0 } ?: connection.contentLengthLong
            tempFile.parentFile?.mkdirs()
            connection.inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var downloadedBytes = 0L
                    var lastPercent = -1
                    while (true) {
                        coroutineContext.ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        digest.update(buffer, 0, read)
                        downloadedBytes += read
                        if (totalBytes > 0) {
                            val percent = (downloadedBytes * 100 / totalBytes).toInt().coerceIn(0, 100)
                            if (percent != lastPercent) {
                                lastPercent = percent
                                onProgress(percent)
                            }
                        }
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun verifyDigest(digest: MessageDigest, expectedSha256: String) {
        val actual = digest.digest().joinToString("") { byte -> "%02x".format(byte.toInt() and 0xFF) }
        if (!actual.equals(expectedSha256, ignoreCase = true)) {
            throw UpdateDownloadException(UpdateDownloadFailure.DigestMismatch)
        }
    }

    private fun Throwable.toFailure(): UpdateDownloadFailure = when (this) {
        is SocketTimeoutException -> UpdateDownloadFailure.Timeout
        is FileNotFoundException -> UpdateDownloadFailure.Storage
        else -> UpdateDownloadFailure.Network
    }

    private const val SHA256_ALGORITHM = "SHA-256"
    private const val CONNECT_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 15_000
    private const val BUFFER_SIZE = 8 * 1024
}

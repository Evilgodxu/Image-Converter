package com.tpgszhq.jh.screens.settings.component.output

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri

/**
 * 输出目录辅助类，用于处理 SAF URI 并提取可读的文件夹路径信息
 */
object OutputDirectoryHelper {

    /**
     * 从 SAF URI 中提取可读的文件夹路径显示
     *
     * @return 可读的文件夹路径，如 "/storage/emulated/0/DCIM/Camera"；无法解析时返回简化后的 URI
     */
    fun getReadablePath(context: Context, uriString: String?): String? {
        if (uriString.isNullOrEmpty()) return null

        val uri = uriString.toUri()

        // 优先从 DocumentsContract 解析真实路径，失败时退回简化的 URI 展示
        return getPathFromDocumentUri(context, uri) ?: simplifyUri(uri)
    }

    /**
     * 从 Document URI 解析路径
     */
    private fun getPathFromDocumentUri(context: Context, uri: Uri): String? {
        try {
            // 检查是否是 DocumentsProvider 的 URI
            if (DocumentsContract.isTreeUri(uri)) {
                return parseDocumentPath(DocumentsContract.getTreeDocumentId(uri))
            } else if (DocumentsContract.isDocumentUri(context, uri)) {
                return parseDocumentPath(DocumentsContract.getDocumentId(uri))
            }
        } catch (e: Exception) {
            // 解析失败，返回 null
        }
        return null
    }

    /**
     * 解析文档 ID，转换为真实路径
     * 例如："primary:DCIM/Camera" -> "/storage/emulated/0/DCIM/Camera"
     */
    private fun parseDocumentPath(documentId: String?): String? {
        if (documentId.isNullOrEmpty()) return null

        val parts = documentId.split(":", limit = 2)
        val storageType = parts.getOrNull(0) ?: return documentId
        val path = parts.getOrNull(1) ?: ""

        val storagePath = when (storageType.lowercase()) {
            "primary" -> "/storage/emulated/0"
            "home" -> "/storage/emulated/0"
            else -> "/storage/$storageType"
        }

        return if (path.isNotEmpty()) "$storagePath/$path" else storagePath
    }

    /**
     * 简化 URI 显示，移除 scheme、authority 与 /tree/ 前缀
     */
    private fun simplifyUri(uri: Uri): String {
        val path = uri.path ?: return uri.toString()

        return try {
            java.net.URLDecoder.decode(path, "UTF-8")
                .replace(Regex("^/(tree|document)/"), "")
        } catch (e: Exception) {
            path
        }
    }
}

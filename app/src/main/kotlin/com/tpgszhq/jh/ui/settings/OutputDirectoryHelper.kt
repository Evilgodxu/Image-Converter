package com.tpgszhq.jh.ui.settings

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract

/**
 * 输出目录辅助类，用于处理 SAF URI 并提取可读的文件夹路径信息
 */
object OutputDirectoryHelper {

    /**
     * 从 SAF URI 中提取可读的文件夹路径显示
     *
     * @param context 上下文
     * @param uriString URI 字符串
     * @return 可读的文件夹路径，如 "/storage/emulated/0/DCIM/Camera" 或原始 URI（无法解析时）
     */
    fun getReadablePath(context: Context, uriString: String?): String? {
        if (uriString.isNullOrEmpty()) return null

        val uri = Uri.parse(uriString)

        // 尝试从 DocumentsContract 解析路径
        val pathFromDocument = getPathFromDocumentUri(context, uri)
        if (pathFromDocument != null) {
            return pathFromDocument
        }

        // 如果无法解析，返回简化后的 URI
        return simplifyUri(uri)
    }

    /**
     * 从 Document URI 解析路径
     */
    private fun getPathFromDocumentUri(context: Context, uri: Uri): String? {
        try {
            // 检查是否是 DocumentsProvider 的 URI
            if (DocumentsContract.isTreeUri(uri)) {
                // 获取文档树信息
                val documentId = DocumentsContract.getTreeDocumentId(uri)
                return parseDocumentPath(documentId)
            } else if (DocumentsContract.isDocumentUri(context, uri)) {
                val documentId = DocumentsContract.getDocumentId(uri)
                return parseDocumentPath(documentId)
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

        return if (path.isNotEmpty()) {
            "$storagePath/$path"
        } else {
            storagePath
        }
    }

    /**
     * 简化 URI 显示，移除 scheme 和 authority
     */
    private fun simplifyUri(uri: Uri): String {
        val path = uri.path ?: return uri.toString()
        
        // 尝试解码路径中的特殊字符
        return try {
            val decodedPath = java.net.URLDecoder.decode(path, "UTF-8")
            // 移除开头的 /tree/ 或 /document/
            decodedPath.replace(Regex("^/(tree|document)/"), "")
        } catch (e: Exception) {
            path
        }
    }

    /**
     * 获取文件夹名称（用于简短显示）
     */
    fun getFolderName(context: Context, uriString: String?): String? {
        val fullPath = getReadablePath(context, uriString) ?: return null
        
        // 返回最后一级文件夹名称
        return fullPath.substringAfterLast("/", fullPath)
    }
}

package com.tpgszhq.jh.ui.settings

import android.content.Context
import android.net.Uri

data class SettingsUiState(
    val isLoading: Boolean = true,
    val themeMode: String = "system",
    val language: String = "system",
    val outputDirectory: String? = null,
) {
    val outputDirectoryUri: Uri? = outputDirectory?.let { Uri.parse(it) }
    val hasOutputDirectory: Boolean = outputDirectory != null

    /**
     * 获取可读的输出目录路径
     */
    fun getReadableOutputDirectory(context: Context): String? {
        return OutputDirectoryHelper.getReadablePath(context, outputDirectory)
    }
}

package com.tpgszhq.jh.screens.home

import android.net.Uri
import androidx.core.net.toUri

// 转换状态：空闲 / 转换中（含进度）/ 成功 / 失败
sealed interface ConvertStatus {
    data object Idle : ConvertStatus

    data class Converting(val current: Int, val total: Int) : ConvertStatus

    data class Success(val outputUris: List<Uri>) : ConvertStatus

    data class Error(val message: String) : ConvertStatus
}

// 转换动作：无 / 请求补选输出目录（转换前发现未设置目录）
sealed interface ConvertAction {
    data object None : ConvertAction

    data object RequestOutputDirectory : ConvertAction
}

// 待转换图片：临时文件 URI + 原始文件名 + 相对目录（用于在输出目录中还原层级）
data class ImageInfo(
    val uri: Uri,
    val name: String,
    val relativePath: String = "",
)

data class HomeUiState(
    val isScanningDirectory: Boolean = false,
    val selectedImages: List<ImageInfo> = emptyList(),
    val currentImageIndex: Int = 0,
    val convertStatus: ConvertStatus = ConvertStatus.Idle,
    val selectedTab: String = TAB_FORMAT,
    val selectedFormat: String = "JPG",
    val quality: Int = 85,
    val outputDirectory: String? = null,
    val convertAction: ConvertAction = ConvertAction.None,
    val showPickerDialog: Boolean = false,
) {
    val hasImages: Boolean = selectedImages.isNotEmpty()
    val imageCount: Int = selectedImages.size
    val isConverting: Boolean = convertStatus is ConvertStatus.Converting
    val hasOutputDirectory: Boolean = outputDirectory != null
    val outputDirectoryUri: Uri? = outputDirectory?.toUri()

    companion object {
        const val TAB_FORMAT = "format"
        const val TAB_QUALITY = "quality"
    }
}

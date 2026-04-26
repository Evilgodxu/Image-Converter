package com.tpgszhq.jh.ui.home

import android.net.Uri

sealed interface ConvertStatus {
    data object Idle : ConvertStatus
    data class Converting(val current: Int, val total: Int) : ConvertStatus
    data class Success(val outputUris: List<Uri>) : ConvertStatus
    data class Error(val message: String) : ConvertStatus
}

sealed interface ConvertAction {
    data object None : ConvertAction
    data object RequestOutputDirectory : ConvertAction
}

data class ImageInfo(
    val uri: Uri,
    val name: String,
    val relativePath: String = "",
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val isScanningDirectory: Boolean = false,
    val selectedImages: List<ImageInfo> = emptyList(),
    val currentImageIndex: Int = 0,
    val convertStatus: ConvertStatus = ConvertStatus.Idle,
    val selectedFormat: String = "JPG",
    val quality: Int = 85,
    val outputDirectory: String? = null,
    val convertAction: ConvertAction = ConvertAction.None,
    val sourceTreeUri: String? = null,
) {
    val currentImage: ImageInfo? = selectedImages.getOrNull(currentImageIndex)
    val hasImages: Boolean = selectedImages.isNotEmpty()
    val imageCount: Int = selectedImages.size
    val isConverting: Boolean = convertStatus is ConvertStatus.Converting
    val hasOutputDirectory: Boolean = outputDirectory != null
    val outputDirectoryUri: Uri? = outputDirectory?.let { Uri.parse(it) }
}

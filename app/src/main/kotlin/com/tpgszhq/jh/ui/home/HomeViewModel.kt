package com.tpgszhq.jh.ui.home

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tpgszhq.jh.R
import com.tpgszhq.jh.data.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayDeque

class HomeViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val outputDirectory = userPreferencesRepository.outputDirectory.first()
            _uiState.value = _uiState.value.copy(outputDirectory = outputDirectory)
        }
    }

    fun setSelectedImages(context: Context, uris: List<Uri>) {
        viewModelScope.launch {
            // 清理旧临时文件
            clearTempImages(context)

            val imageInfos = withContext(Dispatchers.IO) {
                uris.mapNotNull { uri ->
                    val name = getFileNameFromUri(context, uri)

                    try {
                        // 尝试申请持久化权限
                        val contentResolver = context.contentResolver
                        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        contentResolver.takePersistableUriPermission(uri, takeFlags)

                        // 验证URI是否可访问
                        val canRead = try {
                            contentResolver.openInputStream(uri)?.close()
                            true
                        } catch (e: Exception) {
                            false
                        }

                        if (canRead) {
                            ImageInfo(
                                uri = uri,
                                name = name,
                            )
                        } else {
                            // 如果无法直接访问，复制到临时目录
                            copyImageToTemp(context, uri, name)?.let { tempUri ->
                                ImageInfo(
                                    uri = tempUri,
                                    name = name,
                                )
                            }
                        }
                    } catch (e: Exception) {
                        // 持久化权限申请失败，复制到临时目录
                        copyImageToTemp(context, uri, name)?.let { tempUri ->
                            ImageInfo(
                                uri = tempUri,
                                name = name,
                            )
                        }
                    }
                }
            }

            _uiState.value = _uiState.value.copy(
                selectedImages = imageInfos,
                currentImageIndex = 0,
                convertStatus = ConvertStatus.Idle,
                sourceTreeUri = null,
            )
        }
    }

    // 设置从外部传入的图片（从文件管理器打开或分享）
    fun setExternalImages(context: Context, uris: List<Uri>) {
        viewModelScope.launch {
            val imageInfos = withContext(Dispatchers.IO) {
                // 先清理旧临时文件
                clearTempImages(context)

                uris.mapNotNull { uri ->
                    val name = getFileNameFromUri(context, uri)
                    if (name.isEmpty() || name == "unknown") return@mapNotNull null

                    try {
                        // 尝试申请持久化权限
                        val contentResolver = context.contentResolver
                        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        contentResolver.takePersistableUriPermission(uri, takeFlags)

                        // 验证URI是否可访问
                        val canRead = try {
                            contentResolver.openInputStream(uri)?.close()
                            true
                        } catch (e: Exception) {
                            false
                        }

                        if (canRead) {
                            // 持久化权限申请成功且可访问，直接使用原URI
                            ImageInfo(
                                uri = uri,
                                name = name,
                            )
                        } else {
                            // 无法访问，复制到临时目录
                            copyImageToTemp(context, uri, name)?.let { tempUri ->
                                ImageInfo(
                                    uri = tempUri,
                                    name = name,
                                )
                            }
                        }
                    } catch (e: Exception) {
                        // 分享传入的URI不支持持久化权限，需要立即复制到临时目录
                        copyImageToTemp(context, uri, name)?.let { tempUri ->
                            ImageInfo(
                                uri = tempUri,
                                name = name,
                            )
                        }
                    }
                }
            }

            if (imageInfos.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    selectedImages = imageInfos,
                    currentImageIndex = 0,
                    convertStatus = ConvertStatus.Idle,
                    sourceTreeUri = null,
                )
            }
        }
    }

    // 清理临时文件目录
    private fun clearTempImages(context: Context) {
        try {
            // 清理新旧临时目录
            listOf("shared_images", "temp_images").forEach { dirName ->
                val tempDir = File(context.cacheDir, dirName)
                if (tempDir.exists()) {
                    tempDir.listFiles()?.forEach { file ->
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            // 清理失败不影响主流程
        }
    }

    // 将图片复制到应用临时目录
    private fun copyImageToTemp(context: Context, sourceUri: Uri, fileName: String): Uri? {
        return try {
            val tempDir = File(context.cacheDir, "temp_images").apply {
                if (!exists()) mkdirs()
            }
            // 生成唯一文件名避免冲突
            val uniqueFileName = if (fileName.contains(".")) {
                val extension = fileName.substringAfterLast(".")
                val baseName = fileName.substringBeforeLast(".")
                "${baseName}_${System.currentTimeMillis()}.$extension"
            } else {
                "${fileName}_${System.currentTimeMillis()}"
            }
            val tempFile = File(tempDir, uniqueFileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            Uri.fromFile(tempFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 将分享的图片复制到应用临时目录（兼容旧方法名）
    private fun copySharedImageToTemp(context: Context, sourceUri: Uri, fileName: String): Uri? {
        return copyImageToTemp(context, sourceUri, fileName)
    }

    // 从目录加载图片（使用BFS批量查询，在后台线程执行）
    fun loadImagesFromDirectory(context: Context, treeUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanningDirectory = true)

            val images = withContext(Dispatchers.IO) {
                // 清理旧临时文件
                clearTempImages(context)

                getImagesFromDirectoryBfs(context, treeUri)
            }

            _uiState.value = _uiState.value.copy(
                selectedImages = images,
                currentImageIndex = 0,
                convertStatus = ConvertStatus.Idle,
                isScanningDirectory = false,
                sourceTreeUri = treeUri.toString(),
            )
        }
    }

    fun nextImage() {
        val currentState = _uiState.value
        if (currentState.selectedImages.isNotEmpty()) {
            val nextIndex = (currentState.currentImageIndex + 1) % currentState.selectedImages.size
            _uiState.value = currentState.copy(currentImageIndex = nextIndex)
        }
    }

    fun previousImage() {
        val currentState = _uiState.value
        if (currentState.selectedImages.isNotEmpty()) {
            val prevIndex = if (currentState.currentImageIndex == 0) {
                currentState.selectedImages.size - 1
            } else {
                currentState.currentImageIndex - 1
            }
            _uiState.value = currentState.copy(currentImageIndex = prevIndex)
        }
    }

    fun clearImages() {
        _uiState.value = _uiState.value.copy(
            selectedImages = emptyList(),
            currentImageIndex = 0,
            convertStatus = ConvertStatus.Idle,
            sourceTreeUri = null,
        )
    }

    fun setCurrentImageIndex(index: Int) {
        val currentState = _uiState.value
        if (index in 0 until currentState.selectedImages.size) {
            _uiState.value = currentState.copy(currentImageIndex = index)
        }
    }

    fun setSelectedFormat(format: String) {
        _uiState.value = _uiState.value.copy(selectedFormat = format)
    }

    fun setQuality(quality: Int) {
        _uiState.value = _uiState.value.copy(quality = quality)
    }

    fun requestConvert() {
        val currentState = _uiState.value
        if (!currentState.hasOutputDirectory) {
            _uiState.value = currentState.copy(convertAction = ConvertAction.RequestOutputDirectory)
        } else {
            _uiState.value = currentState.copy(convertAction = ConvertAction.None)
        }
    }

    fun setOutputDirectory(uri: Uri) {
        viewModelScope.launch {
            val uriString = uri.toString()
            userPreferencesRepository.setOutputDirectory(uriString)
            _uiState.value = _uiState.value.copy(
                outputDirectory = uriString,
                convertAction = ConvertAction.None,
            )
        }
    }

    fun clearConvertAction() {
        _uiState.value = _uiState.value.copy(convertAction = ConvertAction.None)
    }

    // 批量转换所有图片
    fun convertAllImages(context: Context) {
        val currentState = _uiState.value
        val images = currentState.selectedImages
        val outputDirUri = currentState.outputDirectoryUri ?: return

        if (images.isEmpty()) return

        viewModelScope.launch {
            val outputUris = mutableListOf<Uri>()
            var hasError = false
            var errorMessage = ""

            images.forEachIndexed { index, imageInfo ->
                _uiState.value = currentState.copy(
                    convertStatus = ConvertStatus.Converting(current = index + 1, total = images.size)
                )

                val result = withContext(Dispatchers.IO) {
                    try {
                        val bitmap = loadBitmap(context, imageInfo.uri)
                            ?: return@withContext Result.failure<Uri>(Exception(context.getString(R.string.error_load_image, imageInfo.name)))

                        // 构建输出文件路径（保持原文件名和目录结构）
                        val originalName = imageInfo.name
                        val baseName = originalName.substringBeforeLast(".", originalName)
                        val newExtension = currentState.selectedFormat.lowercase()
                        val outputFileName = "$baseName.$newExtension"

                        val outputUri = saveBitmapToDirectory(
                            context = context,
                            treeUri = outputDirUri,
                            bitmap = bitmap,
                            format = currentState.selectedFormat,
                            quality = currentState.quality,
                            fileName = outputFileName,
                            relativePath = imageInfo.relativePath,
                        )

                        if (outputUri != null) {
                            Result.success(outputUri)
                        } else {
                            Result.failure(Exception(context.getString(R.string.error_convert_image, imageInfo.name)))
                        }
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                }

                if (result.isSuccess) {
                    outputUris.add(result.getOrThrow())
                } else {
                    hasError = true
                    errorMessage = result.exceptionOrNull()?.message ?: context.getString(R.string.error_unknown)
                }
            }

            _uiState.value = currentState.copy(
                convertStatus = when {
                    hasError -> ConvertStatus.Error(errorMessage)
                    else -> ConvertStatus.Success(outputUris)
                }
            )
        }
    }

    fun clearConvertStatus() {
        _uiState.value = _uiState.value.copy(convertStatus = ConvertStatus.Idle)
    }

    private fun loadBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            // 首先尝试直接读取
            val bitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }

            if (bitmap != null) {
                return bitmap
            }

            // 如果直接读取失败且是content URI，尝试通过文件描述符读取
            if (uri.scheme == "content") {
                try {
                    context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                        BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)
                    }
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun saveBitmapToDirectory(
        context: Context,
        treeUri: Uri,
        bitmap: Bitmap,
        format: String,
        quality: Int,
        fileName: String,
        relativePath: String = "",
    ): Uri? {
        val compressFormat = when (format.uppercase()) {
            "PNG" -> Bitmap.CompressFormat.PNG
            "WEBP" -> Bitmap.CompressFormat.WEBP
            else -> Bitmap.CompressFormat.JPEG
        }

        return try {
            val contentResolver = context.contentResolver
            val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)

            // 获取或创建子目录
            var currentDirDocId = rootDocId
            if (relativePath.isNotEmpty()) {
                val pathParts = relativePath.split("/").filter { it.isNotEmpty() }
                for (part in pathParts) {
                    currentDirDocId = findOrCreateDirectory(contentResolver, treeUri, currentDirDocId, part)
                        ?: return null
                }
            }

            // 在目标目录创建文件
            val dirUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, currentDirDocId)
            val mimeType = when (format.uppercase()) {
                "PNG" -> "image/png"
                "WEBP" -> "image/webp"
                else -> "image/jpeg"
            }

            val newFileUri = DocumentsContract.createDocument(
                contentResolver,
                dirUri,
                mimeType,
                fileName,
            ) ?: return null

            contentResolver.openOutputStream(newFileUri)?.use { outputStream ->
                bitmap.compress(compressFormat, quality, outputStream)
            }

            newFileUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 查找或创建子目录
    private fun findOrCreateDirectory(
        contentResolver: android.content.ContentResolver,
        treeUri: Uri,
        parentDocId: String,
        dirName: String,
    ): String? {
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, parentDocId)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)

        // 先查找是否已存在
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
        )

        contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)

            while (cursor.moveToNext()) {
                val docId = cursor.getString(idColumn)
                val name = cursor.getString(nameColumn)
                val mimeType = cursor.getString(mimeTypeColumn)

                if (name == dirName && mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                    return docId
                }
            }
        }

        // 不存在则创建
        return try {
            DocumentsContract.createDocument(
                contentResolver,
                parentUri,
                DocumentsContract.Document.MIME_TYPE_DIR,
                dirName,
            )?.let { newDirUri ->
                DocumentsContract.getDocumentId(newDirUri)
            }
        } catch (e: Exception) {
            null
        }
    }

    // 使用BFS（广度优先搜索）批量查询目录中的所有图片
    private fun getImagesFromDirectoryBfs(context: Context, treeUri: Uri): List<ImageInfo> {
        val images = mutableListOf<ImageInfo>()
        val contentResolver = context.contentResolver

        // 使用队列进行BFS遍历，存储 Pair<documentId, relativePath>
        val directoryQueue = ArrayDeque<Pair<String, String>>()

        val rootDocId = try {
            DocumentsContract.getTreeDocumentId(treeUri)
        } catch (e: Exception) {
            return emptyList()
        }
        directoryQueue.add(Pair(rootDocId, ""))

        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        )

        while (directoryQueue.isNotEmpty()) {
            val (currentDocId, currentRelativePath) = directoryQueue.removeFirst()

            try {
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                    treeUri,
                    currentDocId,
                )

                contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val mimeTypeColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    val nameColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)

                    while (cursor.moveToNext()) {
                        val documentId = cursor.getString(idColumn)
                        val mimeType = cursor.getString(mimeTypeColumn)
                        val displayName = cursor.getString(nameColumn)

                        when {
                            mimeType?.startsWith("image/") == true -> {
                                val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                                images.add(ImageInfo(
                                    uri = documentUri,
                                    name = displayName,
                                    relativePath = currentRelativePath,
                                ))
                            }
                            mimeType == DocumentsContract.Document.MIME_TYPE_DIR -> {
                                val newRelativePath = if (currentRelativePath.isEmpty()) {
                                    displayName
                                } else {
                                    "$currentRelativePath/$displayName"
                                }
                                directoryQueue.add(Pair(documentId, newRelativePath))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                continue
            }
        }

        return images
    }

    // 从URI获取文件名
    private fun getFileNameFromUri(context: Context?, uri: Uri): String {
        if (context == null) return "unknown"

        var fileName = context.getString(R.string.default_file_name)
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex) ?: "unknown"
                }
            }
        }
        return fileName
    }
}

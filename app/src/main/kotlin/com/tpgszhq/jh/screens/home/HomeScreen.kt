package com.tpgszhq.jh.screens.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tpgszhq.jh.LocalAppContainer
import com.tpgszhq.jh.screens.home.compact.HomeCompactAssembly
import com.tpgszhq.jh.screens.home.component.picker.ImagePickerDialog
import com.tpgszhq.jh.screens.home.expanded.HomeExpandedAssembly
import com.tpgszhq.jh.update.LocalAppUpdateViewModel
import com.tpgszhq.jh.windowsize.WindowSizeClass
import com.tpgszhq.jh.windowsize.rememberWindowSizeClass

// 页面入口：装配状态与事件、承载外部图片导入与 SAF 选择器副作用，按窗口尺寸类分发形态，不含布局
@Composable
fun HomeScreen(
    externalImageUris: List<Uri>,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val container = LocalAppContainer.current
    val updateViewModel = LocalAppUpdateViewModel.current
    // 页面级 ViewModel：作用域跟随导航条目，随页面出栈回收
    val factory = remember(container) {
        viewModelFactory { initializer { HomeViewModel(container.settingsRepository) } }
    }
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 进入首页触发每日首次自动检查；发现新版本交由应用级更新对话框呈现，自动检查本身静默
    LaunchedEffect(Unit) { updateViewModel.checkDailyOnce() }

    // 从文件管理器打开或分享进入时，把外部 URI 复制到临时目录再预览
    LaunchedEffect(externalImageUris) {
        if (externalImageUris.isNotEmpty()) {
            viewModel.setExternalImages(context, externalImageUris)
        }
    }

    // 已保存的输出目录可能已被撤销授权，读取到目录后校验一次
    LaunchedEffect(uiState.outputDirectory) {
        if (uiState.outputDirectory != null) {
            viewModel.validateOutputDirectory(context)
        }
    }

    // 输出目录选择器：仅在转换前发现未设置目录时拉起，选定后立即开始转换
    val outputDirectoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { treeUri ->
        treeUri?.let { uri ->
            takePersistableWritePermission(context, uri)
            viewModel.setOutputDirectory(uri)
            viewModel.convertAllImages(context)
        } ?: run {
            // 用户取消选择，清除转换动作
            viewModel.clearConvertAction()
        }
    }

    LaunchedEffect(uiState.convertAction) {
        if (uiState.convertAction is ConvertAction.RequestOutputDirectory) {
            outputDirectoryPicker.launch(null)
        }
    }

    val multipleFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.setSelectedImages(context, uris)
        }
    }

    val directoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { treeUri ->
        treeUri?.let { viewModel.loadImagesFromDirectory(context, it) }
    }

    val onConvert: () -> Unit = {
        if (uiState.hasOutputDirectory) {
            viewModel.convertAllImages(context)
        } else {
            viewModel.requestConvert()
        }
    }

    when (rememberWindowSizeClass()) {
        WindowSizeClass.Compact -> HomeCompactAssembly(
            uiState = uiState,
            onOpenSettings = onOpenSettings,
            onShowPicker = viewModel::showImagePicker,
            onTabChange = viewModel::setSelectedTab,
            onFormatChange = viewModel::setSelectedFormat,
            onQualityChange = viewModel::setQuality,
            onConvert = onConvert,
            onImageIndexChange = viewModel::setCurrentImageIndex,
            modifier = modifier,
        )

        WindowSizeClass.Medium, WindowSizeClass.Expanded -> HomeExpandedAssembly(
            uiState = uiState,
            onOpenSettings = onOpenSettings,
            onShowPicker = viewModel::showImagePicker,
            onTabChange = viewModel::setSelectedTab,
            onFormatChange = viewModel::setSelectedFormat,
            onQualityChange = viewModel::setQuality,
            onConvert = onConvert,
            onImageIndexChange = viewModel::setCurrentImageIndex,
            modifier = modifier,
        )
    }

    if (uiState.showPickerDialog) {
        ImagePickerDialog(
            onDismiss = viewModel::dismissImagePicker,
            onSelectMultiple = {
                viewModel.dismissImagePicker()
                multipleFilePicker.launch(arrayOf("image/*"))
            },
            onSelectDirectory = {
                viewModel.dismissImagePicker()
                directoryPicker.launch(null)
            },
        )
    }
}

// 申请输出目录的持久化读写权限；失败不阻断流程，后续写入会按需重新授权
private fun takePersistableWritePermission(context: Context, uri: Uri) {
    try {
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, takeFlags)
    } catch (e: Exception) {
        // 权限获取失败，继续尝试使用
    }
}

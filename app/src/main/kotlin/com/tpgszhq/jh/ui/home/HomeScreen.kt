package com.tpgszhq.jh.ui.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tpgszhq.jh.R
import com.tpgszhq.jh.ui.adaptive.rememberWindowSizeInfo
import com.tpgszhq.jh.ui.localization.stringResource
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    externalImageUris: List<Uri> = emptyList(),
    viewModel: HomeViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val windowSizeInfo = rememberWindowSizeInfo()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val topBarInsets = if (windowSizeInfo.isCompact) {
        WindowInsets.statusBars
    } else {
        WindowInsets(0, 0, 0, 0)
    }
    var selectedTab by remember { mutableStateOf("format") }
    var showPickerDialog by remember { mutableStateOf(false) }

    // 处理外部传入的图片URI（从文件管理器打开或分享）
    LaunchedEffect(externalImageUris) {
        if (externalImageUris.isNotEmpty()) {
            viewModel.setExternalImages(context, externalImageUris)
        }
    }

    // 验证输出目录权限
    LaunchedEffect(Unit) {
        if (uiState.outputDirectory != null) {
            viewModel.validateOutputDirectory(context)
        }
    }

    // 输出目录选择器（首次转换时使用）
    val outputDirectoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { treeUri ->
        treeUri?.let { uri ->
            // 获取持久化权限
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                // 权限获取失败，继续尝试使用
            }
            viewModel.setOutputDirectory(uri)
            // 设置目录后自动开始转换
            viewModel.convertAllImages(context)
        } ?: run {
            // 用户取消选择，清除转换动作
            viewModel.clearConvertAction()
        }
    }

    // 处理转换动作
    LaunchedEffect(uiState.convertAction) {
        when (uiState.convertAction) {
            is ConvertAction.RequestOutputDirectory -> {
                outputDirectoryPicker.launch(null)
            }
            else -> {}
        }
    }

    // 显示转换结果提示
    LaunchedEffect(uiState.convertStatus) {
        when (uiState.convertStatus) {
            is ConvertStatus.Success -> {
                // 可以在这里显示成功提示或分享转换后的图片
            }
            is ConvertStatus.Error -> {
                // 错误处理
            }
            else -> {}
        }
    }

    // 多选文件启动器
    val multipleFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.setSelectedImages(context, uris)
        }
    }

    // 选择目录启动器（用于选择图片）
    val directoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { treeUri ->
        treeUri?.let {
            // 使用 ViewModel 在后台线程加载图片
            viewModel.loadImagesFromDirectory(context, it)
        }
    }

    if (showPickerDialog) {
        Dialog(
            onDismissRequest = { showPickerDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = {
                            showPickerDialog = false
                            multipleFilePicker.launch(arrayOf("image/*"))
                        },
                    ) {
                        Text(
                            text = stringResource(R.string.home_select_multiple),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    TextButton(
                        onClick = {
                            showPickerDialog = false
                            directoryPicker.launch(null)
                        },
                    ) {
                        Text(
                            text = stringResource(R.string.home_select_directory),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                        )
                    }
                },
                windowInsets = topBarInsets,
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(innerPadding)
            .padding(innerPadding)

        if (windowSizeInfo.isCompact) {
            // 手机竖屏：单列布局
            CompactHomeContent(
                uiState = uiState,
                selectedTab = selectedTab,
                onTabChange = { selectedTab = it },
                onShowPicker = { showPickerDialog = true },
                onFormatChange = { viewModel.setSelectedFormat(it) },
                onQualityChange = { viewModel.setQuality(it) },
                onConvert = {
                    if (uiState.hasOutputDirectory) {
                        viewModel.convertAllImages(context)
                    } else {
                        viewModel.requestConvert()
                    }
                },
                onImageIndexChange = { viewModel.setCurrentImageIndex(it) },
                modifier = contentModifier,
            )
        } else {
            // 横屏/平板：双列布局
            ExpandedHomeContent(
                uiState = uiState,
                selectedTab = selectedTab,
                onTabChange = { selectedTab = it },
                onShowPicker = { showPickerDialog = true },
                onFormatChange = { viewModel.setSelectedFormat(it) },
                onQualityChange = { viewModel.setQuality(it) },
                onConvert = {
                    if (uiState.hasOutputDirectory) {
                        viewModel.convertAllImages(context)
                    } else {
                        viewModel.requestConvert()
                    }
                },
                onImageIndexChange = { viewModel.setCurrentImageIndex(it) },
                modifier = contentModifier,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CompactHomeContent(
    uiState: HomeUiState,
    selectedTab: String,
    onTabChange: (String) -> Unit,
    onShowPicker: () -> Unit,
    onFormatChange: (String) -> Unit,
    onQualityChange: (Int) -> Unit,
    onConvert: () -> Unit,
    onImageIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // 图片预览区
            ImagePreviewCard(
                uiState = uiState,
                onShowPicker = onShowPicker,
                onImageIndexChange = onImageIndexChange,
                modifier = Modifier.size(240.dp),
            )

            // 图片计数
            if (uiState.hasImages) {
                Text(
                    text = stringResource(R.string.home_current_image, uiState.currentImageIndex + 1, uiState.imageCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // 控制面板
            ControlPanel(
                uiState = uiState,
                selectedTab = selectedTab,
                onTabChange = onTabChange,
                onFormatChange = onFormatChange,
                onQualityChange = onQualityChange,
                onConvert = onConvert,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExpandedHomeContent(
    uiState: HomeUiState,
    selectedTab: String,
    onTabChange: (String) -> Unit,
    onShowPicker: () -> Unit,
    onFormatChange: (String) -> Unit,
    onQualityChange: (Int) -> Unit,
    onConvert: () -> Unit,
    onImageIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier.padding(24.dp),
    ) {
        // 左侧：图片预览
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            ImagePreviewCard(
                uiState = uiState,
                onShowPicker = onShowPicker,
                onImageIndexChange = onImageIndexChange,
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .aspectRatio(1f),
            )

            // 图片计数
            if (uiState.hasImages) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.home_current_image, uiState.currentImageIndex + 1, uiState.imageCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // 右侧：控制面板
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
        ) {
            ControlPanel(
                uiState = uiState,
                selectedTab = selectedTab,
                onTabChange = onTabChange,
                onFormatChange = onFormatChange,
                onQualityChange = onQualityChange,
                onConvert = onConvert,
                modifier = Modifier.fillMaxWidth(0.9f),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImagePreviewCard(
    uiState: HomeUiState,
    onShowPicker: () -> Unit,
    onImageIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(enabled = !uiState.isConverting && !uiState.isScanningDirectory) {
                onShowPicker()
            },
        contentAlignment = Alignment.Center,
    ) {
        if (uiState.hasImages) {
            val pagerState = rememberPagerState(
                initialPage = uiState.currentImageIndex,
                pageCount = { uiState.imageCount },
            )

            LaunchedEffect(uiState.currentImageIndex) {
                if (pagerState.currentPage != uiState.currentImageIndex) {
                    pagerState.animateScrollToPage(uiState.currentImageIndex)
                }
            }

            LaunchedEffect(pagerState.currentPage) {
                if (pagerState.currentPage != uiState.currentImageIndex) {
                    onImageIndexChange(pagerState.currentPage)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !uiState.isConverting,
            ) { page ->
                val imageInfo = uiState.selectedImages[page]
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageInfo.uri)
                        .crossfade(true)
                        .diskCachePolicy(coil3.request.CachePolicy.DISABLED)
                        .memoryCachePolicy(coil3.request.CachePolicy.DISABLED)
                        .build(),
                    contentDescription = stringResource(R.string.home_preview_description),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            // 转换中加载遮罩
            if (uiState.isConverting) {
                val status = uiState.convertStatus as? ConvertStatus.Converting
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 4.dp,
                        )
                        Text(
                            text = status?.let {
                                stringResource(
                                    R.string.home_converting_progress,
                                    it.current,
                                    it.total
                                )
                            } ?: stringResource(R.string.home_converting),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(R.string.home_preview_description),
                modifier = Modifier.fillMaxSize(0.5f),
                contentScale = ContentScale.Fit,
            )
        }

        // 扫描目录加载遮罩
        if (uiState.isScanningDirectory) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 4.dp,
                    )
                    Text(
                        text = stringResource(R.string.home_scanning),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlPanel(
    uiState: HomeUiState,
    selectedTab: String,
    onTabChange: (String) -> Unit,
    onFormatChange: (String) -> Unit,
    onQualityChange: (Int) -> Unit,
    onConvert: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        // 格式/质量切换
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            SegmentedButton(
                selected = selectedTab == "format",
                onClick = { onTabChange("format") },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                icon = {},
            ) {
                Text(stringResource(R.string.home_tab_format))
            }
            SegmentedButton(
                selected = selectedTab == "quality",
                onClick = { onTabChange("quality") },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                icon = {},
            ) {
                Text(stringResource(R.string.home_tab_quality))
            }
        }

        // 格式选择或质量调节
        if (selectedTab == "format") {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth(),
            ) {
                SegmentedButton(
                    selected = uiState.selectedFormat == "JPG",
                    onClick = { onFormatChange("JPG") },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                    icon = {},
                    enabled = !uiState.isConverting,
                ) {
                    Text(stringResource(R.string.home_format_jpg))
                }
                SegmentedButton(
                    selected = uiState.selectedFormat == "PNG",
                    onClick = { onFormatChange("PNG") },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                    icon = {},
                    enabled = !uiState.isConverting,
                ) {
                    Text(stringResource(R.string.home_format_png))
                }
                SegmentedButton(
                    selected = uiState.selectedFormat == "WEBP",
                    onClick = { onFormatChange("WEBP") },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                    icon = {},
                    enabled = !uiState.isConverting,
                ) {
                    Text(stringResource(R.string.home_format_webp))
                }
            }
        } else {
            QualitySlider(
                quality = uiState.quality,
                onQualityChange = onQualityChange,
                enabled = !uiState.isConverting,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // 转换结果提示
        when (val status = uiState.convertStatus) {
            is ConvertStatus.Success -> {
                Text(
                    text = stringResource(R.string.home_convert_success),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            is ConvertStatus.Error -> {
                Text(
                    text = status.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            else -> {}
        }

        // 转换按钮
        Button(
            onClick = onConvert,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = uiState.hasImages && !uiState.isConverting && !uiState.isScanningDirectory,
        ) {
            if (uiState.isConverting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(stringResource(R.string.home_convert))
            }
        }
    }
}

@Composable
private fun QualitySlider(
    quality: Int,
    onQualityChange: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 数值显示
        Text(
            text = stringResource(R.string.home_quality_label, quality),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 步进控制 + 滑动条
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // 减小按钮
            IconButton(
                onClick = { if (quality > 10) onQualityChange(quality - 1) },
                enabled = enabled && quality > 10,
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = null,
                )
            }

            // 滑动条
            Slider(
                value = quality.toFloat(),
                onValueChange = { onQualityChange(it.toInt()) },
                valueRange = 10f..100f,
                modifier = Modifier.weight(1f),
                enabled = enabled,
            )

            // 增加按钮
            IconButton(
                onClick = { if (quality < 100) onQualityChange(quality + 1) },
                enabled = enabled && quality < 100,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                )
            }
        }

        // 刻度标记
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.home_quality_worst),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.home_quality_best),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

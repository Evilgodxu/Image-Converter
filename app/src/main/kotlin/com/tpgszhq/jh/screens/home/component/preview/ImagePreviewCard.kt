package com.tpgszhq.jh.screens.home.component.preview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tpgszhq.jh.R
import com.tpgszhq.jh.screens.home.ConvertStatus
import com.tpgszhq.jh.screens.home.HomeUiState

// 图片预览卡：无图时展示占位图，有图时横向分页预览；点击唤起图片来源选择
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImagePreviewCard(
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

            // 状态 → 分页：外部索引变化时同步翻页
            LaunchedEffect(uiState.currentImageIndex) {
                if (pagerState.currentPage != uiState.currentImageIndex) {
                    pagerState.animateScrollToPage(uiState.currentImageIndex)
                }
            }

            // 分页 → 状态：手动滑动后回写索引
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
                    // 预览的源文件随时可能被改写，禁用缓存保证展示始终为当前内容
                    model = ImageRequest.Builder(context)
                        .data(imageInfo.uri)
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .build(),
                    contentDescription = stringResource(R.string.home_preview_description),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            if (uiState.isConverting) {
                val status = uiState.convertStatus as? ConvertStatus.Converting
                LoadingOverlay(
                    text = status?.let {
                        stringResource(R.string.home_converting_progress, it.current, it.total)
                    } ?: stringResource(R.string.home_converting),
                )
            }
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(R.string.home_preview_description),
                modifier = Modifier.fillMaxSize(0.5f),
                contentScale = ContentScale.Fit,
            )
        }

        if (uiState.isScanningDirectory) {
            LoadingOverlay(text = stringResource(R.string.home_scanning))
        }
    }
}

package com.tpgszhq.jh.screens.home.expanded

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tpgszhq.jh.R
import com.tpgszhq.jh.screens.home.HomeUiState
import com.tpgszhq.jh.screens.home.component.control.ControlPanel
import com.tpgszhq.jh.screens.home.component.preview.ImagePreviewCard
import com.tpgszhq.jh.ui.component.AppTopBar
import com.tpgszhq.jh.ui.icons.AppIcons

// 首页宽屏组装器：左预览、右控制面板的双列布局
@Composable
fun HomeExpandedAssembly(
    uiState: HomeUiState,
    onOpenSettings: () -> Unit,
    onShowPicker: () -> Unit,
    onTabChange: (String) -> Unit,
    onFormatChange: (String) -> Unit,
    onQualityChange: (Int) -> Unit,
    onConvert: () -> Unit,
    onImageIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.home_title),
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = AppIcons.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                        )
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .padding(innerPadding)
                .padding(24.dp),
        ) {
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

                if (uiState.hasImages) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(
                            R.string.home_current_image,
                            uiState.currentImageIndex + 1,
                            uiState.imageCount,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

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
                    onTabChange = onTabChange,
                    onFormatChange = onFormatChange,
                    onQualityChange = onQualityChange,
                    onConvert = onConvert,
                    modifier = Modifier.fillMaxWidth(0.9f),
                )
            }
        }
    }
}

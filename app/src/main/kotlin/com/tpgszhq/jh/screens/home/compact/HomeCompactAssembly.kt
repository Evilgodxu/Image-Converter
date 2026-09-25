package com.tpgszhq.jh.screens.home.compact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

// 首页窄屏组装器：全宽单列，预览居中、控制面板在下
@Composable
fun HomeCompactAssembly(
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                ImagePreviewCard(
                    uiState = uiState,
                    onShowPicker = onShowPicker,
                    onImageIndexChange = onImageIndexChange,
                    modifier = Modifier.size(240.dp),
                )

                if (uiState.hasImages) {
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

                ControlPanel(
                    uiState = uiState,
                    onTabChange = onTabChange,
                    onFormatChange = onFormatChange,
                    onQualityChange = onQualityChange,
                    onConvert = onConvert,
                )
            }
        }
    }
}

package com.tpgszhq.jh.screens.home.component.control

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tpgszhq.jh.R
import com.tpgszhq.jh.screens.home.ConvertStatus
import com.tpgszhq.jh.screens.home.HomeUiState

// 格式值：与 Bitmap.CompressFormat 选择保持一一对应
private val FORMATS = listOf("JPG", "PNG", "WEBP")

// 转换控制面板：格式/质量切换 + 结果反馈 + 转换按钮
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlPanel(
    uiState: HomeUiState,
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
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = uiState.selectedTab == HomeUiState.TAB_FORMAT,
                onClick = { onTabChange(HomeUiState.TAB_FORMAT) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                icon = {},
            ) {
                Text(stringResource(R.string.home_tab_format))
            }
            SegmentedButton(
                selected = uiState.selectedTab == HomeUiState.TAB_QUALITY,
                onClick = { onTabChange(HomeUiState.TAB_QUALITY) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                icon = {},
            ) {
                Text(stringResource(R.string.home_tab_quality))
            }
        }

        if (uiState.selectedTab == HomeUiState.TAB_FORMAT) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                FORMATS.forEachIndexed { index, format ->
                    SegmentedButton(
                        selected = uiState.selectedFormat == format,
                        onClick = { onFormatChange(format) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = FORMATS.size),
                        icon = {},
                        enabled = !uiState.isConverting,
                    ) {
                        Text(
                            when (format) {
                                "JPG" -> stringResource(R.string.home_format_jpg)
                                "PNG" -> stringResource(R.string.home_format_png)
                                else -> stringResource(R.string.home_format_webp)
                            }
                        )
                    }
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

            else -> Unit
        }

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

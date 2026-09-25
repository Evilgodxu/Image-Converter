package com.tpgszhq.jh.screens.home.component.control

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tpgszhq.jh.R
import com.tpgszhq.jh.ui.icons.AppIcons

// 质量调节：当前值 + 步进按钮 + 滑动条 + 两端刻度
@Composable
fun QualitySlider(
    quality: Int,
    onQualityChange: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.home_quality_label, quality),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconButton(
                onClick = { if (quality > MIN_QUALITY) onQualityChange(quality - 1) },
                enabled = enabled && quality > MIN_QUALITY,
            ) {
                Icon(
                    imageVector = AppIcons.Remove,
                    contentDescription = stringResource(R.string.home_quality_decrease),
                )
            }

            Slider(
                value = quality.toFloat(),
                onValueChange = { onQualityChange(it.toInt()) },
                valueRange = MIN_QUALITY.toFloat()..MAX_QUALITY.toFloat(),
                modifier = Modifier.weight(1f),
                enabled = enabled,
            )

            IconButton(
                onClick = { if (quality < MAX_QUALITY) onQualityChange(quality + 1) },
                enabled = enabled && quality < MAX_QUALITY,
            ) {
                Icon(
                    imageVector = AppIcons.Add,
                    contentDescription = stringResource(R.string.home_quality_increase),
                )
            }
        }

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

private const val MIN_QUALITY = 10
private const val MAX_QUALITY = 100

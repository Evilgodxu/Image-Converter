package com.tpgszhq.jh.screens.home.component.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tpgszhq.jh.R

// 图片来源选择弹窗：多选文件 或 整目录导入
@Composable
fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onSelectMultiple: () -> Unit,
    onSelectDirectory: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
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
                TextButton(onClick = onSelectMultiple) {
                    Text(
                        text = stringResource(R.string.home_select_multiple),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                TextButton(onClick = onSelectDirectory) {
                    Text(
                        text = stringResource(R.string.home_select_directory),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

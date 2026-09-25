package com.tpgszhq.jh.screens.settings.component.output

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tpgszhq.jh.R
import com.tpgszhq.jh.ui.component.SectionCard
import com.tpgszhq.jh.ui.icons.AppIcons

// 输出目录设置项：已设置时展示当前路径并可更改/清除，未设置时引导选择
@Composable
fun OutputDirectory(
    outputDirectory: String?,
    onSelectDirectory: () -> Unit,
    onClearDirectory: () -> Unit,
) {
    val context = LocalContext.current
    SectionCard(title = stringResource(R.string.settings_output_directory_title)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (outputDirectory != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.settings_output_directory_current),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = OutputDirectoryHelper.getReadablePath(context, outputDirectory).orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onSelectDirectory,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.settings_output_directory_change))
                        }
                        OutlinedButton(
                            onClick = onClearDirectory,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.settings_output_directory_clear))
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.settings_output_directory_not_set),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(
                        onClick = onSelectDirectory,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = AppIcons.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_output_directory_select))
                    }
                }
            }
        }
    }
}

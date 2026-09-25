package com.tpgszhq.jh.screens.privacy.component.action

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tpgszhq.jh.R

// 隐私页操作区：拒绝（左）与同意（右）
@Composable
fun PrivacyActions(
    onReject: () -> Unit,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedButton(
            onClick = onReject,
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.privacy_reject))
        }
        Button(
            onClick = onAccept,
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.privacy_accept))
        }
    }
}

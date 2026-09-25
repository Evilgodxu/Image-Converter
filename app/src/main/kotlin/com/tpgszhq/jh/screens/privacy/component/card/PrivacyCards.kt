package com.tpgszhq.jh.screens.privacy.component.card

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tpgszhq.jh.R
import com.tpgszhq.jh.ui.icons.AppIcons

// 应用权限说明
@Composable
fun PermissionCard(modifier: Modifier = Modifier) {
    InfoCard(
        icon = AppIcons.Info,
        iconTint = MaterialTheme.colorScheme.primary,
        title = stringResource(R.string.privacy_permissions_title),
        content = stringResource(R.string.privacy_permissions_content),
        modifier = modifier,
    )
}

// 数据收集说明
@Composable
fun DataCollectionCard(modifier: Modifier = Modifier) {
    InfoCard(
        icon = AppIcons.CheckCircle,
        iconTint = MaterialTheme.colorScheme.secondary,
        title = stringResource(R.string.privacy_data_collection_title),
        content = stringResource(R.string.privacy_data_collection_content),
        modifier = modifier,
    )
}

// 网络连接说明
@Composable
fun NetworkCard(modifier: Modifier = Modifier) {
    InfoCard(
        icon = AppIcons.Warning,
        iconTint = MaterialTheme.colorScheme.tertiary,
        title = stringResource(R.string.privacy_network_title),
        content = stringResource(R.string.privacy_network_content),
        modifier = modifier,
    )
}

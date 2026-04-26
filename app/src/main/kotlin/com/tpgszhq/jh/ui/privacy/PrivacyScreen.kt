package com.tpgszhq.jh.ui.privacy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tpgszhq.jh.R
import com.tpgszhq.jh.ui.adaptive.rememberWindowSizeInfo
import com.tpgszhq.jh.ui.localization.stringResource
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    onPrivacyAccepted: () -> Unit,
    onPrivacyRejected: () -> Unit,
    viewModel: PrivacyViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val windowSizeInfo = rememberWindowSizeInfo()

    LaunchedEffect(uiState.privacyAccepted) {
        if (uiState.privacyAccepted && !uiState.isLoading) {
            onPrivacyAccepted()
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val topBarInsets = if (windowSizeInfo.isCompact) {
        WindowInsets.statusBars
    } else {
        WindowInsets(0, 0, 0, 0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.privacy_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                windowInsets = topBarInsets,
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(innerPadding)
            .padding(innerPadding)

        if (windowSizeInfo.isExpanded) {
            // 平板/大屏布局：双列卡片 + 底部按钮，支持滚动
            Column(
                modifier = contentModifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    PermissionCard(modifier = Modifier.weight(1f))
                    DataCollectionCard(modifier = Modifier.weight(1f))
                }
                NetworkCard(modifier = Modifier.fillMaxWidth())
                PrivacyButtons(
                    onReject = { viewModel.rejectPrivacy(onPrivacyRejected) },
                    onAccept = { viewModel.acceptPrivacy(onPrivacyAccepted) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            // 手机布局：单列可滚动
            Column(
                modifier = contentModifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PermissionCard(modifier = Modifier.fillMaxWidth())
                DataCollectionCard(modifier = Modifier.fillMaxWidth())
                NetworkCard(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.weight(1f))
                PrivacyButtons(
                    onReject = { viewModel.rejectPrivacy(onPrivacyRejected) },
                    onAccept = { viewModel.acceptPrivacy(onPrivacyAccepted) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.privacy_permissions_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = stringResource(R.string.privacy_permissions_content),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun DataCollectionCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = stringResource(R.string.privacy_data_collection_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = stringResource(R.string.privacy_data_collection_content),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun NetworkCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    text = stringResource(R.string.privacy_network_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = stringResource(R.string.privacy_network_content),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PrivacyButtons(onReject: () -> Unit, onAccept: () -> Unit, modifier: Modifier = Modifier) {
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

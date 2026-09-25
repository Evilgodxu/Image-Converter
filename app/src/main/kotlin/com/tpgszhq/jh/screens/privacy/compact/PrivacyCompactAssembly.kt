package com.tpgszhq.jh.screens.privacy.compact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tpgszhq.jh.R
import com.tpgszhq.jh.screens.privacy.component.action.PrivacyActions
import com.tpgszhq.jh.screens.privacy.component.card.DataCollectionCard
import com.tpgszhq.jh.screens.privacy.component.card.NetworkCard
import com.tpgszhq.jh.screens.privacy.component.card.PermissionCard
import com.tpgszhq.jh.ui.component.AppTopBar

// 隐私页窄屏组装器：单列滚动说明，操作区固定在底部
@Composable
fun PrivacyCompactAssembly(
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = stringResource(R.string.privacy_title)) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PermissionCard(modifier = Modifier.fillMaxWidth())
                DataCollectionCard(modifier = Modifier.fillMaxWidth())
                NetworkCard(modifier = Modifier.fillMaxWidth())
            }

            PrivacyActions(
                onReject = onReject,
                onAccept = onAccept,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )
        }
    }
}

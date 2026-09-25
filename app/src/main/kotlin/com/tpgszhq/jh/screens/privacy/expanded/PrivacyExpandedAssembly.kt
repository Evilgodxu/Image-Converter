package com.tpgszhq.jh.screens.privacy.expanded

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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

// 隐私页宽屏组装器：权限与数据收集并排，网络说明与操作区通栏
@Composable
fun PrivacyExpandedAssembly(
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
            PrivacyActions(
                onReject = onReject,
                onAccept = onAccept,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

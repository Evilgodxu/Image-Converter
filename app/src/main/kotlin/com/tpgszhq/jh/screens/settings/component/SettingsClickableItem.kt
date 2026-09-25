package com.tpgszhq.jh.screens.settings.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 设置页专属：可点击列表项。无需坐标时走 clickable（完整语义/焦点/键盘）；
// 需要返回点击坐标时经 detectTapGestures 同步触发以保证位置准确，
// 并补充按钮语义与焦点以支持无障碍与方向键导航
@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    onClickWithPosition: ((Offset) -> Unit)? = null,
) {
    var currentCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { currentCoordinates = it }
            .then(
                if (onClickWithPosition == null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                        .pointerInput(onClickWithPosition) {
                            detectTapGestures { offset ->
                                onClickWithPosition(
                                    currentCoordinates?.localToRoot(offset) ?: offset,
                                )
                            }
                        }
                        .semantics { role = Role.Button }
                        .focusable()
                },
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

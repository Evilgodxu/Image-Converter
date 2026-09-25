package com.tpgszhq.jh.screens.settings.component.info

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.tpgszhq.jh.R
import com.tpgszhq.jh.log.CrashLogManager
import com.tpgszhq.jh.ui.icons.AppIcons

// 关于：品牌、版本（点击检查更新）、分享今日崩溃日志与项目主页
@Composable
fun AppInfo(version: String, onCheckForUpdate: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_about_brand),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.settings_version, version),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
                .clickable(onClick = onCheckForUpdate),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
        Text(
            text = stringResource(R.string.settings_log),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clickable { shareTodayLog(context) },
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .clickable {
                        val urlIntent = Intent(Intent.ACTION_VIEW, GITHUB_URL.toUri())
                        context.startActivity(urlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = AppIcons.Code,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = GITHUB_URL,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }
    }
}

// 唤起系统分享今日异常日志；今日无日志时提示用户
private fun shareTodayLog(context: Context) {
    val logFile = CrashLogManager.getTodayLogFile()
    if (logFile == null) {
        Toast.makeText(context, context.getString(R.string.settings_log_empty_today), Toast.LENGTH_SHORT).show()
        return
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", logFile)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    // 本地化上下文非 Activity，启动外部 Activity 需 NEW_TASK 标志
    val chooser = Intent.createChooser(shareIntent, context.getString(R.string.settings_share_log_title))
    context.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

private const val GITHUB_URL = "https://github.com/Evilgodxu/Image-Converter"

package com.tpgszhq.jh.update

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tpgszhq.jh.MainActivity
import com.tpgszhq.jh.R

// 更新下载通知：渠道创建、进度/完成/失败三种通知，以及统一的发布与撤销
internal object UpdateNotifications {

    internal const val NOTIFICATION_ID = 1001

    private const val CHANNEL_ID = "app_update"
    private const val REQUEST_OPEN_APP = 100
    private const val PENDING_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.update_notification_channel),
            NotificationManager.IMPORTANCE_LOW,
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun progress(context: Context, percent: Int): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_update_notification)
            .setContentTitle(context.getString(R.string.update_notification_downloading))
            .setContentText(context.getString(R.string.update_notification_progress, percent))
            .setProgress(PROGRESS_MAX, percent, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppIntent(context))
            .build()

    fun completed(context: Context, installIntent: Intent): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_update_notification)
            .setContentTitle(context.getString(R.string.update_notification_completed))
            .setContentText(context.getString(R.string.update_notification_completed_hint))
            .setAutoCancel(true)
            .setContentIntent(PendingIntent.getActivity(context, REQUEST_OPEN_APP, installIntent, PENDING_FLAGS))
            .build()

    fun failed(context: Context): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_update_notification)
            .setContentTitle(context.getString(R.string.update_notification_failed))
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(context))
            .build()

    // 通知权限被拒绝时系统会静默丢弃通知，不会抛异常，故此处无需再判权限
    @SuppressLint("MissingPermission")
    fun post(context: Context, notification: Notification) {
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun openAppIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            REQUEST_OPEN_APP,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PENDING_FLAGS,
        )

    private const val PROGRESS_MAX = 100
}

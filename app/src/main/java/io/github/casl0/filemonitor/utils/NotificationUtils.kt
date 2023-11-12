/*
 * Copyright 2023 CASL0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.casl0.filemonitor.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.casl0.filemonitor.R

/** 通知チャネルを作成します */
internal fun NotificationManager.makeNotificationChannel(
    channelId: CharSequence,
    channelName: CharSequence,
) {
    if (Build.VERSION.SDK_INT >= 26) {
        val notificationChannel = NotificationChannel(
            channelId.toString(),
            channelName,
            NotificationManager.IMPORTANCE_LOW,
        )
        createNotificationChannel(notificationChannel)
    }
}

/**
 * 通知を作成します
 *
 * @param channelId 通知チャネル
 * @param notificationId 通知ID
 * @param title タイトル
 * @param message メッセージ
 */
@RequiresPermission(value = Manifest.permission.POST_NOTIFICATIONS)
internal fun Context.makeNotification(
    channelId: CharSequence,
    notificationId: Int,
    title: CharSequence,
    message: CharSequence,
) {
    val builder = NotificationCompat.Builder(this, channelId.toString())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))

    NotificationManagerCompat.from(this).notify(notificationId, builder.build())
}

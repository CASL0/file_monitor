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

package io.github.casl0.filemonitor.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.FileObserver
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import io.github.casl0.filemonitor.R
import io.github.casl0.filemonitor.utils.makeNotificationChannel
import java.io.File

/** ファイル監視サービス */
class FileMonitoringService : Service() {
    /** サービスのバインド */
    inner class FileMonitoringBinder : Binder() {
        /** サービス */
        val service: FileMonitoringService = this@FileMonitoringService
    }

    /** バインダー */
    private val binder = FileMonitoringBinder()

    /** ファイル監視用インスタンス */
    private var fileObserver: FileObserver? = null

    /** 監視中のファイル */
    var monitoredFile: File? = null
        private set

    /** 監視中であるか */
    val monitoringNow: Boolean
        get() = monitoredFile != null

    /** 監視開始 */
    @Suppress("DEPRECATION")
    fun start(file: File, onFileChange: (FileObserverEvent, String?) -> Unit) {
        Log.d(TAG, "START MONITORING")
        fileObserver = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            object : FileObserver(file) {
                override fun onEvent(event: Int, path: String?) {
                    try {
                        onFileChange(event.toFileObserverEvent(), path)
                    } catch (e: NoSuchElementException) {
                        Log.d(TAG, "UNKNOWN EVENT: $event")
                    }
                }
            }
        } else {
            object : FileObserver(file.absolutePath) {
                override fun onEvent(event: Int, path: String?) {
                    try {
                        onFileChange(event.toFileObserverEvent(), path)
                    } catch (e: NoSuchElementException) {
                        Log.d(TAG, "UNKNOWN EVENT: $event")
                    }
                }
            }
        }.also {
            it.startWatching()
            fileObserver?.stopWatching()
        }
        updateForegroundService(getString(R.string.file_monitor_enabled))
        monitoredFile = file
    }

    /** ファイル監視終了 */
    fun stop() {
        Log.d(TAG, "STOP MONITORING")
        fileObserver?.stopWatching()
        updateForegroundService(getString(R.string.file_monitor_disabled))
        monitoredFile = null
    }

    //region android.app.Service
    override fun onCreate() {
        super.onCreate()
        updateForegroundService(getString(R.string.file_monitor_disabled))
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }
    //endregion

    //region Private Methods
    /**
     * フォアグラウンドサービスを通知する関数
     *
     * @param message 通知に表示するメッセージ
     */
    private fun updateForegroundService(message: CharSequence) {
        Log.d(TAG, "updateForegroundService - $message")
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).run {
            makeNotificationChannel(
                CHANNEL_ID,
                getString(R.string.file_monitoring_channel_name),
            )
        }
        startForeground(
            NOTIFICATION_ID,
            NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentText(message)
                    .build(),
        )
    }
    //endregion
}

/** [FileObserver]の定数のenum class */
enum class FileObserverEvent(val rawValue: Int) {
    ACCESS(rawValue = FileObserver.ACCESS),
    ALL_EVENTS(rawValue = FileObserver.ALL_EVENTS),
    ATTRIB(rawValue = FileObserver.ATTRIB),
    CLOSE_NOWRITE(rawValue = FileObserver.CLOSE_NOWRITE),
    CLOSE_WRITE(rawValue = FileObserver.CLOSE_WRITE),
    CREATE(rawValue = FileObserver.CREATE),
    DELETE(rawValue = FileObserver.DELETE),
    DELETE_SELF(rawValue = FileObserver.DELETE_SELF),
    MODIFY(rawValue = FileObserver.MODIFY),
    MOVED_FROM(rawValue = FileObserver.MOVED_FROM),
    MOVED_TO(rawValue = FileObserver.MOVED_TO),
    MOVE_SELF(rawValue = FileObserver.MOVE_SELF),
    OPEN(rawValue = FileObserver.OPEN),
}

private val fileObserverEvent = FileObserverEvent.values().toList()

/**
 * 生のIntを[FileObserverEvent]に変換します
 *
 * @return 変換後の値
 * @throws [NoSuchElementException]
 */
@Throws(NoSuchElementException::class)
private fun Int.toFileObserverEvent(): FileObserverEvent {
    return fileObserverEvent.first { it.rawValue == this }
}

private const val TAG = "FileMonitoringService"
private const val CHANNEL_ID = "file_monitoring"
private const val NOTIFICATION_ID = 100

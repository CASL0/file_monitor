package io.github.casl0.filemonitor

import android.Manifest
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import io.github.casl0.filemonitor.service.FileMonitoringService
import io.github.casl0.filemonitor.service.FileObserverEvent
import io.github.casl0.filemonitor.ui.home.HomeScreen
import io.github.casl0.filemonitor.ui.theme.FileMonitorTheme
import io.github.casl0.filemonitor.utils.PermissionResult
import io.github.casl0.filemonitor.utils.askPermissions
import io.github.casl0.filemonitor.utils.makeNotification
import io.github.casl0.filemonitor.utils.makeNotificationChannel
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    /** ファイル監視サービス */
    private var fileMonitoringService: FileMonitoringService? = null

    /** ViewModel */
    private val viewModel: MainViewModel by viewModels()

    /** パーミッションの要求 */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        Log.d(TAG, "Permission Result: $it")
        if (it.values.any { isGranted -> !isGranted }) {
            viewModel.showPermissionRationale(true)
        }
    }

    //region android.app.Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectService()
        lifecycleScope.launch {
            viewModel.uiState.collect {
                if (it.monitoringNow) {
                    fileMonitoringService?.start(
                        File(it.monitoredDir),
                        this@MainActivity::onFileChange,
                    )
                } else {
                    fileMonitoringService?.stop()
                }
            }
        }
        setContent {
            FileMonitorTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        askMediaPermissions()
    }
    //endregion

    //region Private Methods
    /** ファイル変更コールバック */
    private fun onFileChange(event: FileObserverEvent, path: String?) {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).run {
            makeNotificationChannel(
                FILE_CHANGE_NOTIFICATION_CHANNEL,
                getString(R.string.file_change),
            )
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                makeNotification(
                    FILE_CHANGE_NOTIFICATION_CHANNEL,
                    FILE_CHANGE_NOTIFICATION_ID,
                    getString(R.string.file_change),
                    "$path -- $event",
                )
            }
        }
    }

    /** ファイル監視サービスに接続します */
    private fun connectService() {
        Intent(this, FileMonitoringService::class.java).also {
            bindService(
                it,
                object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                        Log.d(TAG, "onServiceConnected, ${name.toString()}, ${service.toString()}")
                        val binder = service as FileMonitoringService.FileMonitoringBinder
                        fileMonitoringService = binder.service
                        viewModel.serviceConnected()
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        Log.d(TAG, "onServiceDisconnected, ${name.toString()}")
                    }
                },
                Context.BIND_AUTO_CREATE,
            )
        }
    }

    /** MEDIAアクセスのパーミッション要求 */
    private fun askMediaPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
            )
        } else {
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        when (askPermissions(permissions)) {
            PermissionResult.GRANTED                                  -> {
                Log.d(TAG, "Permission granted: $permissions")
            }

            PermissionResult.SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE -> {
                viewModel.showPermissionRationale(true)
            }

            PermissionResult.NOT_GRANTED                              -> {
                requestPermissionLauncher.launch(permissions.toTypedArray())
            }
        }
    }
    //endregion
}

private const val TAG = "MainActivity"
private const val FILE_CHANGE_NOTIFICATION_CHANNEL = "file_change"
private const val FILE_CHANGE_NOTIFICATION_ID = 1000

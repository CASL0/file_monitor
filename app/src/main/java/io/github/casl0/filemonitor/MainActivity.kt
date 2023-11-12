package io.github.casl0.filemonitor

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import io.github.casl0.filemonitor.service.FileMonitoringService
import io.github.casl0.filemonitor.service.FileObserverEvent
import io.github.casl0.filemonitor.ui.home.HomeScreen
import io.github.casl0.filemonitor.ui.theme.FileMonitorTheme
import io.github.casl0.filemonitor.utils.PermissionResult
import io.github.casl0.filemonitor.utils.askPermissions
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
        if (it.values.any { isGranted -> !isGranted }) {
            TODO("implement")
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
                TODO("implement")
            }

            PermissionResult.NOT_GRANTED                              -> {
                requestPermissionLauncher.launch(permissions.toTypedArray())
            }
        }
    }
    //endregion

    //region Private Methods
    /** ファイル変更コールバック */
    private fun onFileChange(event: FileObserverEvent, path: String?) {
        Toast.makeText(this, "$event -- $path", Toast.LENGTH_SHORT).show()
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
    //endregion
}

private const val TAG = "MainActivity"

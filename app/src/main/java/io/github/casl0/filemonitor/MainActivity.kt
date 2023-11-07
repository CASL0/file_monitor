package io.github.casl0.filemonitor

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.casl0.filemonitor.service.FileMonitoringService
import io.github.casl0.filemonitor.service.FileObserverEvent
import io.github.casl0.filemonitor.ui.theme.FileMonitorTheme

class MainActivity : ComponentActivity() {
    /** ファイル監視サービス */
    private var fileMonitoringService: FileMonitoringService? = null

    /** ViewModel */
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectService()
        setContent {
            FileMonitorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val uiState by viewModel.uiState.collectAsState()

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = {
                            if (uiState.monitoringNow) {
                                fileMonitoringService?.stop()
                                viewModel.enableMonitoring(false)
                            } else {
                                fileMonitoringService?.start(
                                    applicationContext.filesDir,
                                    this@MainActivity::onFileChange,
                                )
                                viewModel.enableMonitoring(true)
                            }
                        }) {
                            Text(
                                text = if (uiState.monitoringNow) "STOP MONITORING" else "START MONITORING"
                            )
                        }

                    }
                }
            }
        }
    }

    /** ファイル変更コールバック */
    private fun onFileChange(event: FileObserverEvent, path: String?) {
        Log.i(TAG, "onFileChange, $event, $path")
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
}

private const val TAG = "MainActivity"

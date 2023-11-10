package io.github.casl0.filemonitor

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import io.github.casl0.filemonitor.service.FileMonitoringService
import io.github.casl0.filemonitor.service.FileObserverEvent
import io.github.casl0.filemonitor.ui.home.HomeScreen
import io.github.casl0.filemonitor.ui.theme.FileMonitorTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    /** ファイル監視サービス */
    private var fileMonitoringService: FileMonitoringService? = null

    /** ViewModel */
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectService()
        lifecycleScope.launch {
            viewModel.uiState.collect {
                if (it.monitoringNow) {
                    fileMonitoringService?.start(
                        it.monitoredDir,
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
}

private const val TAG = "MainActivity"

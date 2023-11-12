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

package io.github.casl0.filemonitor.ui.home

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.casl0.filemonitor.MainViewModel
import io.github.casl0.filemonitor.R
import io.github.casl0.filemonitor.ui.theme.FileMonitorTheme

/** ホーム画面 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    Scaffold(
        modifier = modifier,
        topBar = {
            AppBar(
                toggleEnable = !uiState.permissionRationale,
                checked = uiState.monitoringNow,
                onCheckedChange = viewModel::enableMonitoring,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        if (uiState.permissionRationale) {
            val (message, label) = stringResource(
                id = R.string.permission_rationale
            ) to stringResource(R.string.setting)
            LaunchedEffect(key1 = snackbarHostState) {
                val result = snackbarHostState.showSnackbar(
                    message,
                    label,
                    withDismissAction = false,
                    duration = SnackbarDuration.Indefinite
                )
                when (result) {
                    SnackbarResult.Dismissed -> TODO()
                    SnackbarResult.ActionPerformed -> {
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${context.packageName}")
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }.run {
                            context.startActivity(this)
                        }
                        viewModel.showPermissionRationale(false)
                    }
                }
            }
        }
        Column(modifier = Modifier.padding(it)) {
            TextField(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                value = uiState.monitoredDir,
                onValueChange = viewModel::onMonitoredDirChange,
                readOnly = uiState.monitoringNow,
                label = { Text(text = stringResource(R.string.monitored)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = stringResource(
                            R.string.directory
                        )
                    )
                },
                singleLine = true,
            )
        }
    }
}

/** TopAppBar */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
    toggleEnable: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.app_name))
        },
        modifier = modifier,
        actions = {
            Toggle(enable = toggleEnable, checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}

/** AppBarのトグル */
@Composable
private fun Toggle(
    enable: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Switch(
        enabled = enable,
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.primary,
            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
            uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        thumbContent = if (checked) {
            {
                Icon(
                    imageVector = Icons.Filled.Visibility,
                    contentDescription = "Monitor",
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        } else {
            null
        },
        modifier = modifier
    )
}

@Preview(name = "チェックON時のToggle")
@Composable
private fun TogglePreview() {
    FileMonitorTheme {
        Toggle(enable = true, checked = true, onCheckedChange = {})
    }
}

@Preview(name = "デフォルトのTopAppBar")
@Composable
private fun AppBarPreview() {
    FileMonitorTheme {
        AppBar(toggleEnable = true, checked = false, onCheckedChange = {})
    }
}

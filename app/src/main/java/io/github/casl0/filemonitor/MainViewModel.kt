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

package io.github.casl0.filemonitor

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** MainActivityのUI状態 */
internal data class MainUiState(
    /** サービス接続済み */
    val serviceConnected: Boolean = false,
    /** ファイル監視中 */
    val monitoringNow: Boolean = false,
    /** 監視対象のディレクトリ */
    val monitoredDir: String = "/",
    /** パーミッションの説明表示中 */
    val permissionRationale: Boolean = false,
)

/** MainActivityのビジネスロジックを扱う */
internal class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    /** ファイル監視サービス接続通知 */
    fun serviceConnected() {
        _uiState.update { it.copy(serviceConnected = true) }
    }

    /**
     * ファイル監視状態変更
     *
     * @param enable 監視を有効にした場合はtrue、それ以外はfalse
     */
    fun enableMonitoring(enable: Boolean) {
        _uiState.update { it.copy(monitoringNow = enable) }
    }

    /**
     * 監視対象ディレクトリ更新
     *
     * @param newValue 更新後の値
     */
    fun onMonitoredDirChange(newValue: String) {
        _uiState.update { it.copy(monitoredDir = newValue) }
    }

    /**
     * パーミッション説明を表示
     *
     * @param enable 表示する場合true、それ以外はfalse
     */
    fun showPermissionRationale(enable: Boolean) {
        _uiState.update { it.copy(permissionRationale = enable) }
    }
}

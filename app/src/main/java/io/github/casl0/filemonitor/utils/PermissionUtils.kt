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

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/** パーミッション承認確認時の結果 */
internal enum class PermissionResult {
    GRANTED,
    SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE,
    NOT_GRANTED,
}

/**
 * パーミッションを要求します
 *
 * @param permissions 要求するパーミッションのリスト
 * @return [PermissionResult]
 */
internal fun Activity.askPermissions(
    permissions: List<String>,
): PermissionResult {
    return when {
        permissions.all {
            ContextCompat.checkSelfPermission(
                this, it
            ) == PackageManager.PERMISSION_GRANTED
        }    -> {
            PermissionResult.GRANTED
        }

        permissions.any {
            this.shouldShowRequestPermissionRationale(it)
        }    -> {
            PermissionResult.SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE
        }

        else -> {
            PermissionResult.NOT_GRANTED
        }
    }
}

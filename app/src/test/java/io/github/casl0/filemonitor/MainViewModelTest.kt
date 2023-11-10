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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class MainViewModelTest {

    @Test
    fun serviceConnected() = runTest {
        val viewModel = MainViewModel()

        viewModel.serviceConnected()
        assertThat(viewModel.uiState.first().serviceConnected, `is`(true))
    }

    @Test
    fun enableMonitoring() = runTest {
        val viewModel = MainViewModel()

        viewModel.enableMonitoring(true)
        assertThat(viewModel.uiState.first().monitoringNow, `is`(true))

        viewModel.enableMonitoring(false)
        assertThat(viewModel.uiState.first().monitoringNow, `is`(false))
    }
}

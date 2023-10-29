/*
 * Copyright (C) 2021 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.zipline.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.DEFAULT
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

internal class CoroutineEventLoop(
  private val scope: CoroutineScope,
  private val guestService: GuestService,
) {
  private val jobs = mutableMapOf<Int, Job>()

  fun setTimeout(timeoutId: Int, delayMillis: Int) {
    // This must be DEFAULT to prevent recursion on jobs scheduled with setTimeout(0, ...).
    jobs[timeoutId] = scope.launch(start = DEFAULT) {
      delay(delayMillis.toLong())
      scope.ensureActive() // Necessary as delay() won't detect cancellation if the duration is 0.
      guestService.runJob(timeoutId)
      jobs.remove(timeoutId)
    }
  }

  fun clearTimeout(timeoutId: Int) {
    jobs.remove(timeoutId)?.cancel()
  }
}

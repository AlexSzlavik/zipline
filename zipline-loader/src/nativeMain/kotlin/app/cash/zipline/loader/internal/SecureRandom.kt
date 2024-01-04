/*
 * Copyright (C) 2024 Cash App
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
@file:OptIn(ExperimentalForeignApi::class)

package app.cash.zipline.loader.internal

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.pin
import okio.Buffer
import okio.use
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault

class SecureRandom : Random {
  override fun nextBytes(sink: ByteArray) {
    val pin = sink.pin()
    val bytesPointer = when {
      sink.isNotEmpty() -> pin.addressOf(0)
      else -> null
    }

    val status = SecRandomCopyBytes(
      kSecRandomDefault, sink.size.convert(), bytesPointer
    )
    pin.unpin()

    require(status == errSecSuccess) {
      "failed to generate random bytes."
    }
  }

  override fun nextLong(): Long {
    val bytes = ByteArray(8) // initialize 64 bits
    nextBytes(bytes)

    return Buffer().use { it.write(bytes).readLong() }
  }
}

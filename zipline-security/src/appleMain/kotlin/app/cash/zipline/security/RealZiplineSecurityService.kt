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

package app.cash.zipline.security

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.pin
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault

class RealZiplineSecurityService : ZiplineSecurityService {
  override fun nextSecureRandomBytes(size: Int): ByteArray {
    val result = ByteArray(size)
    val pin = result.pin()
    val bytesPointer = when {
      result.isNotEmpty() -> pin.addressOf(0)
      else -> null
    }

    val status = SecRandomCopyBytes(
      kSecRandomDefault,
      result.size.convert(),
      bytesPointer,
    )
    pin.unpin()

    require(status == errSecSuccess) {
      "failed to generate random bytes."
    }

    return result
  }
}

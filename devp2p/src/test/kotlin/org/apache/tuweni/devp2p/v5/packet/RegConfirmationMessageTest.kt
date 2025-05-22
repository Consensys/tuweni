// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.devp2p.v5.packet

import org.apache.tuweni.devp2p.v5.RegConfirmationMessage
import org.apache.tuweni.v2.bytes.Bytes
import org.apache.tuweni.v2.bytes.Bytes32
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RegConfirmationMessageTest {

  @Test
  fun encodeCreatesValidBytesSequence() {
    val requestId = Bytes.fromHexString("0xC6E32C5E89CAA754")
    val message = RegConfirmationMessage(requestId, Bytes32.fromRandom())

    val encodingResult = message.toRLP()

    val decodingResult = RegConfirmationMessage.create(encodingResult)

    assertEquals(decodingResult.requestId, requestId)
    assertEquals(decodingResult.topic, message.topic)
  }
}

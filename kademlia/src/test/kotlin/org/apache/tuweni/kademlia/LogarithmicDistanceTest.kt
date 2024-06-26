// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.kademlia

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LogarithmicDistanceTest {

  @Test
  fun shouldHaveDistanceZeroToSelf() {
    val a = ByteArray(4) { 56 }
    assertEquals(0, a xorDist a)
  }

  @Test
  fun shouldHaveMaximumDistanceToInverse() {
    val a = byteArrayOf(0x0f, 0x0f, 0x0f, 0x0f)
    val b = byteArrayOf(0xf0.toByte(), 0xf0.toByte(), 0xf0.toByte(), 0xf0.toByte())
    assertEquals(32, a xorDist b)
  }

  @Test
  fun shouldCalculateDistance() {
    assertEquals(1, byteArrayOf(0x00) xorDist byteArrayOf(0x01))
    assertEquals(2, byteArrayOf(0x00) xorDist byteArrayOf(0x02))
    assertEquals(2, byteArrayOf(0x00) xorDist byteArrayOf(0x03))
    assertEquals(3, byteArrayOf(0x00) xorDist byteArrayOf(0x04))
    assertEquals(3, byteArrayOf(0x00) xorDist byteArrayOf(0x05))
    assertEquals(3, byteArrayOf(0x00) xorDist byteArrayOf(0x06))
    assertEquals(4, byteArrayOf(0x00) xorDist byteArrayOf(0x0f))
    assertEquals(8, byteArrayOf(0x00) xorDist byteArrayOf(0xff.toByte()))
  }

  @Test
  fun shouldCompareDistances() {
    assertEquals(-1, byteArrayOf(0x00).xorDistCmp(byteArrayOf(0x01), byteArrayOf(0x02)))
    assertEquals(1, byteArrayOf(0x00).xorDistCmp(byteArrayOf(0x02), byteArrayOf(0x01)))
    assertEquals(0, byteArrayOf(0x00).xorDistCmp(byteArrayOf(0x05), byteArrayOf(0x05)))
  }
}

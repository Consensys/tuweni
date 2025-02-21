// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class Bytes48Test {

  @Test
  void failsWhenWrappingArraySmallerThan48() {
    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> Bytes48.wrap(new byte[47]));
    assertEquals("Expected 48 bytes but got 47", exception.getMessage());
  }

  @Test
  void failsWhenWrappingArrayLargerThan48() {
    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> Bytes48.wrap(new byte[49]));
    assertEquals("Expected 48 bytes but got 49", exception.getMessage());
  }

  @Test
  void rightPadAValueToBytes48() {
    Bytes48 b48 = Bytes48.rightPad(Bytes.of(1, 2, 3));
    assertEquals(48, b48.size());
    for (int i = 3; i < 48; ++i) {
      assertEquals((byte) 0, b48.get(i));
    }
    assertEquals((byte) 1, b48.get(0));
    assertEquals((byte) 2, b48.get(1));
    assertEquals((byte) 3, b48.get(2));
  }

  @Test
  void leftPadAValueToBytes48() {
    Bytes48 b48 = Bytes48.leftPad(Bytes.of(1, 2, 3));
    assertEquals(48, b48.size());
    for (int i = 0; i < 28; ++i) {
      assertEquals((byte) 0, b48.get(i));
    }
    assertEquals((byte) 1, b48.get(45));
    assertEquals((byte) 2, b48.get(46));
    assertEquals((byte) 3, b48.get(47));
  }

  @Test
  void failsWhenLeftPaddingValueLargerThan48() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class, () -> Bytes48.leftPad(MutableBytes.create(49)));
    assertEquals("Expected at most 48 bytes but got 49", exception.getMessage());
  }

  @Test
  void failsWhenRightPaddingValueLargerThan48() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class, () -> Bytes48.rightPad(MutableBytes.create(49)));
    assertEquals("Expected at most 48 bytes but got 49", exception.getMessage());
  }

  @Test
  void hexString() {
    Bytes initial = Bytes48.random();
    assertEquals(initial, Bytes48.fromHexStringLenient(initial.toHexString()));
    assertEquals(initial, Bytes48.fromHexString(initial.toHexString()));
    assertEquals(initial, Bytes48.fromHexStringStrict(initial.toHexString()));
  }

  @Test
  void size() {
    assertEquals(48, Bytes48.random().size());
  }

  @Test
  void not() {
    assertEquals(
        Bytes48.fromHexString(
            "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
        Bytes48.leftPad(Bytes.EMPTY).not());
  }

  @Test
  void wrap() {
    Bytes source = Bytes.random(96);
    Bytes48 value = Bytes48.wrap(source, 2);
    assertEquals(source.slice(2, 48), value);
  }

  @Test
  void leftPad() {
    Bytes48 source = Bytes48.random();
    assertSame(source, Bytes48.leftPad(source));
    assertSame(source, Bytes48.rightPad(source));
  }

  @Test
  void or() {
    Bytes48 one =
        Bytes48.fromHexString(
            "0x0000000000000000000000000000000000000000000000ffffffffffffffffffffffffffffffffffffffffffffffffff");
    Bytes48 two =
        Bytes48.fromHexString(
            "0xffffffffffffffffffffffffffffffffffffffffffffff00000000000000000000000000000000000000000000000000");
    assertEquals(
        Bytes48.fromHexString(
            "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
        one.or(two));
  }

  @Test
  void and() {
    Bytes48 one =
        Bytes48.fromHexString(
            "0x0000000000000000000000000000000000000000000000ffffffffffffffffffffffffffffffffffffffffffffffffff");
    Bytes48 two =
        Bytes48.fromHexString(
            "0xffffffffffffffffffffffffffffffffffffffffffffff00000000000000000000000000000000000000000000000000");
    assertEquals(
        Bytes48.fromHexString(
            "0x000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"),
        one.and(two));
  }
}

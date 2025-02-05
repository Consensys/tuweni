// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;

import io.netty.buffer.Unpooled;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MutableBytesTest {

  @Test
  void testMutableBytesWrap() {
    MutableBytes b = MutableBytes.fromArray(Bytes.fromHexString("deadbeef").toArrayUnsafe(), 1, 3);
    assertEquals(Bytes.fromHexString("adbeef"), b);
  }

  @Test
  void testClear() {
    MutableBytes b = MutableBytes.fromArray(Bytes.fromHexString("deadbeef").toArrayUnsafe());
    assertEquals(Bytes.fromHexString("00000000"), b.clear());
  }

  @Test
  void testFill() {
    MutableBytes b = MutableBytes.create(2);
    assertEquals(Bytes.fromHexString("0x2222"), b.fill((byte) 34));
  }

  @Test
  void testDecrementAndIncrement() {
    MutableBytes b = MutableBytes.create(2);
    assertEquals(Bytes.fromHexString("0x0001"), b.increment());
    assertEquals(Bytes.fromHexString("0x0000"), b.decrement());

    assertEquals(Bytes.fromHexString("0xFFFE"), b.fill((byte) 0xFF).decrement());

    b = MutableBytes.fromArray(Bytes.fromHexString("0x00FF").toArrayUnsafe());
    assertEquals(Bytes.fromHexString("0x0100"), b.increment());
  }

  @Test
  void setLong() {
    MutableBytes b = MutableBytes.create(8);
    b.setLong(0, 256);
    assertEquals(Bytes.fromHexString("0x0000000000000100"), b);
  }

  @Test
  void setInt() {
    MutableBytes b = MutableBytes.create(4);
    b.setInt(0, 256);
    assertEquals(Bytes.fromHexString("0x00000100"), b);
  }

  @Test
  void setIntOverflow() {
    MutableBytes b = MutableBytes.create(2);
    assertThrows(
        IndexOutOfBoundsException.class,
        () -> {
          b.setInt(0, 18096);
        });
  }

  @Test
  void setLongOverflow() {
    MutableBytes b = MutableBytes.create(6);
    assertThrows(
        IndexOutOfBoundsException.class,
        () -> {
          b.setLong(0, Long.MAX_VALUE);
        });
  }

  //  @Test
  //  void wrap32() {
  //    MutableBytes b = MutableBytes.create(32);
  //    assertTrue(b instanceof MutableBytes32);
  //    b = MutableBytes.fromArray(Bytes.random(36).toArrayUnsafe(), 4, 32);
  //    assertTrue(b instanceof MutableBytes32);
  //  }

  @Test
  void wrapEmpty() {
    MutableBytes b = MutableBytes.fromBuffer(Buffer.buffer());
    assertEquals(b.size(), 0);
    b = MutableBytes.fromByteBuf(Unpooled.buffer(0));
    assertEquals(b.size(), 0);
  }

  @Test
  void testHashcodeUpdates() {
    MutableBytes dest = MutableBytes.create(32);
    int hashCode = dest.hashCode();
    dest.set(1, (byte) 123);
    assertNotEquals(hashCode, dest.hashCode());
  }

  @Test
  void testHashcodeUpdatesBuffer() {
    MutableBytes dest = MutableBytes.fromBuffer(Buffer.buffer(new byte[4]));
    int hashCode = dest.hashCode();
    dest.set(1, (byte) 123);
    assertNotEquals(hashCode, dest.hashCode());
  }

  @Test
  void testHashcodeUpdatesByteBuffer() {
    MutableBytes dest = MutableBytes.fromByteBuffer(ByteBuffer.wrap(new byte[4]));
    int hashCode = dest.hashCode();
    dest.set(1, (byte) 123);
    assertNotEquals(hashCode, dest.hashCode());
  }

  @Test
  void testHashcodeUpdatesByteBuf() {
    MutableBytes dest = MutableBytes.fromByteBuf(Unpooled.buffer(4));
    int hashCode = dest.hashCode();
    dest.set(1, (byte) 123);
    assertNotEquals(hashCode, dest.hashCode());
  }

  @ParameterizedTest
  @CsvSource({
    "0x01, 0x00, 1",
    "0x02, 0x01, 1",
    "0x04, 0x01, 2",
    "0x08, 0x01, 3",
    "0x10, 0x01, 4",
    "0xFF, 0x0F, 4",
    "0xFFFF, 0x00FF, 8",
    "0x1234, 0x0123, 4",
    "0x8000, 0x0001, 15",
    "0x321243, 0x000000, 25",
    "0x213211AD, 0x00000213, 20",
    "0x7FFFFFFF, 0x3FFFFFFF, 1",
    "0xFFFFFFFF, 0x0FFFFFFF, 4",
    "0xABCDEF, 0x00ABCD, 8",
    "0x12345678, 0x01234567, 4",
    "0x00, 0x00, 1",
    "0x01, 0x01, 0",
    "0xAA55, 0x0552, 5",
  })
  void shiftRight(String bytesValue, String expected, int shiftBits) {
    MutableBytes value = Bytes.fromHexString(bytesValue).mutableCopy();
    assertEquals(Bytes.fromHexString(expected), value.shiftRight(shiftBits));
  }

  @ParameterizedTest
  @CsvSource({
    "0x80, 0x00, 1",
    "0x40, 0x80, 1",
    "0x20, 0x80, 2",
    "0x10, 0x80, 3",
    "0x08, 0x80, 4",
    "0xFF, 0xF0, 4",
    "0xFFFF, 0xFF00, 8",
    "0x1234, 0x2340, 4",
    "0x0001, 0x8000, 15",
    "0x321243, 0x000000, 25",
    "0x213211AD, 0x1AD00000, 20",
    "0xFFFFFFFE, 0xFFFFFFFC, 1",
    "0xFFFFFFFF, 0xFFFFFFF0, 4",
    "0xABCDEF, 0xCDEF00, 8",
    "0x12345678, 0x23456780, 4",
    "0x00, 0x00, 1",
    "0x80, 0x80, 0",
    "0xAA55, 0x4AA0, 5",
    "0xAA, 0x40, 5"
  })
  void shiftLeft(String bytesValue, String expected, int shiftBits) {
    MutableBytes value = Bytes.fromHexString(bytesValue).mutableCopy();
    assertEquals(Bytes.fromHexString(expected), value.shiftLeft(shiftBits));
  }

  @ParameterizedTest
  @CsvSource({
    "0x0102, 0x00000102, 4",
    "0x0102, 0x0102, 2",
    "0x0102, 0x0102, 0",
    "0x, 0x000000, 3",
    "0x, 0x, 0",
    "0x01, 0x000000000001, 6",
    "0xFF, 0x00FF, 2",
    "0x0000, 0x00000000, 4",
    "0x000000000000000000000000000000000000000000000000000000, 0x0000000000000000000000000000000000000000000000000000000000, 29",
    "0xE000000000E000000000E000000000, 0x0000000000000000000000000000000000E000000000E000000000E000000000, 32",
    "0x123456789ABCDEF0, 0x123456789ABCDEF0, 4"
  })
  void leftPad(String bytesValue, String expected, int size) {
    MutableBytes value = Bytes.fromHexString(bytesValue).mutableCopy();
    assertEquals(Bytes.fromHexString(expected), value.leftPad(size));
  }

  @ParameterizedTest
  @CsvSource({
    "0x0102, 0x01020000, 4",
    "0x0102, 0x0102, 2",
    "0x0102, 0x0102, 0",
    "0x, 0x000000, 3",
    "0x, 0x, 0",
    "0x01, 0x010000000000, 6",
    "0xFF, 0xFF00, 2",
    "0x0000, 0x00000000, 4",
    "0x000000000000000000000000000000000000000000000000000000, 0x0000000000000000000000000000000000000000000000000000000000, 29",
    "0xE000000000E000000000E000000000, 0xE000000000E000000000E0000000000000000000000000000000000000000000, 32",
    "0x123456789ABCDEF0, 0x123456789ABCDEF0, 4"
  })
  void rightPad(String bytesValue, String expected, int size) {
    MutableBytes value = Bytes.fromHexString(bytesValue).mutableCopy();
    assertEquals(Bytes.fromHexString(expected), value.rightPad(size));
  }
}

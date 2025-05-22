// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.rlp;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.tuweni.v2.bytes.Bytes;
import org.apache.tuweni.v2.units.bigints.UInt256;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ByteBufferWriterTest {

  @ParameterizedTest
  @CsvSource({"8203e8, 1000", "830186a0, 100000"})
  void shouldWriteSmallIntegers(String expectedHex, int value) {
    ByteBuffer buffer = ByteBuffer.allocate(64);
    RLP.encodeTo(buffer, writer -> writer.writeInt(value));
    buffer.flip();
    assertEquals(Bytes.fromHexString(expectedHex), Bytes.wrapByteBuffer(buffer));
  }

  @Test
  void shouldWriteLongIntegers() {
    ByteBuffer buffer = ByteBuffer.allocate(64);
    RLP.encodeTo(buffer, writer -> writer.writeLong(100000L));
    buffer.flip();
    assertEquals(Bytes.fromHexString("830186a0"), Bytes.wrapByteBuffer(buffer));
  }

  @Test
  void shouldWriteUInt256Integers() {
    ByteBuffer buffer = ByteBuffer.allocate(64);
    RLP.encodeTo(buffer, writer -> writer.writeUInt256(UInt256.valueOf(100000L)));
    buffer.flip();
    assertEquals(Bytes.fromHexString("830186a0"), Bytes.wrapByteBuffer(buffer));

    buffer.clear();
    RLP.encodeTo(
        buffer,
        writer ->
            writer.writeUInt256(
                UInt256.fromHexString(
                    "0x0400000000000000000000000000000000000000000000000000f100000000ab")));
    buffer.flip();
    assertEquals(
        Bytes.fromHexString("a00400000000000000000000000000000000000000000000000000f100000000ab"),
        Bytes.wrapByteBuffer(buffer));
  }

  @Test
  void shouldWriteBigIntegers() {
    ByteBuffer buffer = ByteBuffer.allocate(64);
    RLP.encodeTo(buffer, writer -> writer.writeBigInteger(BigInteger.valueOf(100000)));
    buffer.flip();
    assertEquals(Bytes.fromHexString("830186a0"), Bytes.wrapByteBuffer(buffer));

    buffer.clear();
    RLP.encodeTo(buffer, writer -> writer.writeBigInteger(BigInteger.valueOf(127).pow(16)));
    buffer.flip();
    assertEquals(
        Bytes.fromHexString("8ee1ceefa5bbd9ed1c97f17a1df801"), Bytes.wrapByteBuffer(buffer));
  }

  @Test
  void shouldWriteEmptyStrings() {
    ByteBuffer buffer = ByteBuffer.allocate(64);
    RLP.encodeTo(buffer, writer -> writer.writeString(""));
    buffer.flip();
    assertEquals(Bytes.fromHexString("80"), Bytes.wrapByteBuffer(buffer));
  }

  @Test
  void shouldWriteOneCharactersStrings() {
    ByteBuffer buffer = ByteBuffer.allocate(64);
    RLP.encodeTo(buffer, writer -> writer.writeString("d"));
    buffer.flip();
    assertEquals(Bytes.fromHexString("64"), Bytes.wrapByteBuffer(buffer));
  }

  @Test
  void shouldWriteStrings() {
    ByteBuffer buffer = ByteBuffer.allocate(64);
    RLP.encodeTo(buffer, writer -> writer.writeString("dog"));
    buffer.flip();
    assertEquals(Bytes.fromHexString("83646f67"), Bytes.wrapByteBuffer(buffer));
  }

  @Test
  void shouldWriteShortLists() {
    List<String> strings =
        Arrays.asList(
            "asdf", "qwer", "zxcv", "asdf", "qwer", "zxcv", "asdf", "qwer", "zxcv", "asdf", "qwer");

    ByteBuffer buffer = ByteBuffer.allocate(64);
    RLP.encodeListTo(buffer, listWriter -> strings.forEach(listWriter::writeString));
    buffer.flip();

    assertEquals(
        Bytes.fromHexString(
            "f784617364668471776572847a78637684617364668471776572847a"
                + "78637684617364668471776572847a78637684617364668471776572"),
        Bytes.wrapByteBuffer(buffer));
  }

  @Test
  void shouldWriteNestedLists() {
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    RLP.encodeListTo(
        buffer,
        listWriter -> {
          listWriter.writeString("asdf");
          listWriter.writeString("qwer");
          for (int i = 30; i >= 0; --i) {
            listWriter.writeList(
                subListWriter -> {
                  subListWriter.writeString("zxcv");
                  subListWriter.writeString("asdf");
                  subListWriter.writeString("qwer");
                });
          }
        });

    buffer.flip();
    assertTrue(
        RLP.<Boolean>decodeList(
            Bytes.wrapByteBuffer(buffer),
            listReader -> {
              assertEquals("asdf", listReader.readString());
              assertEquals("qwer", listReader.readString());

              for (int i = 30; i >= 0; --i) {
                assertTrue(
                    listReader.<Boolean>readList(
                        subListReader -> {
                          assertEquals("zxcv", subListReader.readString());
                          assertEquals("asdf", subListReader.readString());
                          assertEquals("qwer", subListReader.readString());
                          return true;
                        }));
              }

              return true;
            }));
  }

  @Test
  void shouldWritePreviouslyEncodedValues() {
    ByteBuffer buffer = ByteBuffer.allocate(64);
    RLP.encodeTo(buffer, writer -> writer.writeRLP(RLP.encodeByteArray("abc".getBytes(UTF_8))));
    buffer.flip();
    assertEquals("abc", RLP.decodeString(Bytes.wrapByteBuffer(buffer)));
  }
}

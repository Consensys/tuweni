// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes.v2;

import static org.apache.tuweni.bytes.v2.Utils.checkArgument;
import static org.apache.tuweni.bytes.v2.Utils.checkElementIndex;

import java.nio.ByteBuffer;

class ByteBufferWrappingBytes extends Bytes {

  protected final ByteBuffer byteBuffer;
  protected final int offset;
  protected final int length;

  ByteBufferWrappingBytes(ByteBuffer byteBuffer) {
    this(byteBuffer, 0, byteBuffer.limit());
  }

  ByteBufferWrappingBytes(ByteBuffer byteBuffer, int offset, int length) {
    checkArgument(length >= 0, "Invalid negative length");
    int bufferLength = byteBuffer.capacity();
    if (bufferLength > 0) {
      checkElementIndex(offset, bufferLength);
    }
    checkArgument(
        offset + length <= bufferLength,
        "Provided length %s is too big: the value has only %s bytes from offset %s",
        length,
        bufferLength - offset,
        offset);

    this.byteBuffer = byteBuffer;
    this.offset = offset;
    this.length = length;
  }

  @Override
  public int size() {
    return length;
  }

  @Override
  public int getInt(int i) {
    return byteBuffer.getInt(offset + i);
  }

  @Override
  public long getLong(int i) {
    return byteBuffer.getLong(offset + i);
  }

  @Override
  public byte get(int i) {
    return byteBuffer.get(offset + i);
  }

  @Override
  public Bytes slice(int i, int length) {
    if (i == 0 && length == this.length) {
      return this;
    }
    if (length == 0) {
      return Bytes.EMPTY;
    }

    checkElementIndex(i, this.length);
    checkArgument(
        i + length <= this.length,
        "Provided length %s is too big: the value has size %s and has only %s bytes from %s",
        length,
        this.length,
        this.length - i,
        i);

    return new ByteBufferWrappingBytes(byteBuffer, offset + i, length);
  }

  @Override
  public MutableBytes mutableCopy() {
    return MutableBytes.fromByteBuffer(byteBuffer, offset, length);
  }

  @Override
  public void appendTo(ByteBuffer byteBuffer) {
    byteBuffer.put(this.byteBuffer);
  }

  private byte[] toArray() {
    byte[] array = new byte[length];
    for (int i = 0; i < length; i++) {
      array[i] = byteBuffer.get(i + offset);
    }
    return array;
  }

  @Override
  public byte[] toArrayUnsafe() {
    if (!byteBuffer.hasArray()) {
      return toArray();
    }
    byte[] array = byteBuffer.array();
    if (byteBuffer.limit() != length || byteBuffer.arrayOffset() != offset) {
      return toArray();
    }
    return array;
  }

  @Override
  protected void and(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      // TODO: There is a chance for implementing with SIMD - see toArrayUnsafe()
      bytesArray[offset + i] = (byte) (byteBuffer.get(this.offset + i) & bytesArray[offset + i]);
    }
  }

  @Override
  protected void or(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      // TODO: There is a chance for implementing with SIMD - see toArrayUnsafe()
      bytesArray[offset + i] = (byte) (byteBuffer.get(this.offset + i) | bytesArray[offset + i]);
    }
  }

  @Override
  protected void xor(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      // TODO: There is a chance for implementing with SIMD - see toArrayUnsafe()
      bytesArray[offset + i] = (byte) (byteBuffer.get(this.offset + i) ^ bytesArray[offset + i]);
    }
  }
}

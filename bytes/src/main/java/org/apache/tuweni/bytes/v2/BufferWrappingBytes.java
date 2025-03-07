// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes.v2;

import static org.apache.tuweni.bytes.v2.Utils.checkArgument;
import static org.apache.tuweni.bytes.v2.Utils.checkElementIndex;

import io.vertx.core.buffer.Buffer;

class BufferWrappingBytes extends Bytes {

  protected final Buffer buffer;

  BufferWrappingBytes(Buffer buffer) {
    this.buffer = buffer;
  }

  BufferWrappingBytes(Buffer buffer, int offset, int length) {
    checkArgument(length >= 0, "Invalid negative length");
    int bufferLength = buffer.length();
    checkElementIndex(offset, bufferLength + 1);
    checkArgument(
        offset + length <= bufferLength,
        "Provided length %s is too big: the buffer has size %s and has only %s bytes from %s",
        length,
        bufferLength,
        bufferLength - offset,
        offset);

    if (offset == 0 && length == bufferLength) {
      this.buffer = buffer;
    } else {
      this.buffer = buffer.slice(offset, offset + length);
    }
  }

  @Override
  public int size() {
    return buffer.length();
  }

  @Override
  public byte get(int i) {
    return buffer.getByte(i);
  }

  @Override
  public int getInt(int i) {
    return buffer.getInt(i);
  }

  @Override
  public long getLong(int i) {
    return buffer.getLong(i);
  }

  @Override
  public Bytes slice(int i, int length) {
    int size = buffer.length();
    if (i == 0 && length == size) {
      return this;
    }
    if (length == 0) {
      return Bytes.EMPTY;
    }

    checkElementIndex(i, size);
    checkArgument(
        i + length <= size,
        "Provided length %s is too big: the value has size %s and has only %s bytes from %s",
        length,
        size,
        size - i,
        i);

    return new BufferWrappingBytes(buffer.slice(i, i + length));
  }

  @Override
  public MutableBytes mutableCopy() {
    return MutableBytes.fromArray(toArrayUnsafe());
  }

  @Override
  public void appendTo(Buffer buffer) {
    buffer.appendBuffer(this.buffer);
  }

  @Override
  public byte[] toArrayUnsafe() {
    return buffer.getBytes();
  }

  @Override
  protected void and(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      bytesArray[offset + i] = (byte) (buffer.getByte(i) & bytesArray[offset + i]);
    }
  }

  @Override
  protected void or(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      bytesArray[offset + i] = (byte) (buffer.getByte(i) | bytesArray[offset + i]);
    }
  }

  @Override
  protected void xor(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      bytesArray[offset + i] = (byte) (buffer.getByte(i) ^ bytesArray[offset + i]);
    }
  }
}

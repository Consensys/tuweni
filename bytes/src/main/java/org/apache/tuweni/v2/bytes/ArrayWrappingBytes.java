// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.bytes;

import static org.apache.tuweni.v2.bytes.Utils.checkArgument;
import static org.apache.tuweni.v2.bytes.Utils.checkElementIndex;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;

import io.vertx.core.buffer.Buffer;

class ArrayWrappingBytes extends Bytes {

  protected final byte[] bytes;
  protected final int offset;
  protected final int length;

  ArrayWrappingBytes(byte[] bytes) {
    this(bytes, 0, bytes.length);
  }

  ArrayWrappingBytes(byte[] bytes, int offset, int length) {
    checkArgument(length >= 0, "Invalid negative length");
    if (bytes.length > 0) {
      checkElementIndex(offset, bytes.length);
    }
    checkArgument(
        offset + length <= bytes.length,
        "Provided length %s is too big: the value has only %s bytes from offset %s",
        length,
        bytes.length - offset,
        offset);

    this.bytes = bytes;
    this.offset = offset;
    this.length = length;
  }

  @Override
  public int size() {
    return length;
  }

  @Override
  public byte get(int i) {
    // Check bounds because while the array access would throw, the error message would be confusing
    // for the caller.
    checkElementIndex(i, size());
    return bytes[offset + i];
  }

  @Override
  public Bytes slice(int i, int length) {
    if (i == 0 && length == this.length) {
      return this;
    }
    if (length == 0) {
      return EMPTY;
    }

    checkElementIndex(i, this.length);
    checkArgument(
        i + length <= this.length,
        "Provided length %s is too big: the value has size %s and has only %s bytes from %s",
        length,
        this.length,
        this.length - i,
        i);

    return new ArrayWrappingBytes(bytes, offset + i, length);
  }

  @Override
  public int commonPrefixLength(Bytes other) {
    if (!(other instanceof ArrayWrappingBytes o)) {
      return super.commonPrefixLength(other);
    }
    int i = 0;
    while (i < length && i < o.length && bytes[offset + i] == o.bytes[o.offset + i]) {
      i++;
    }
    return i;
  }

  @Override
  public void update(MessageDigest digest) {
    digest.update(bytes, offset, length);
  }

  @Override
  public void appendTo(ByteBuffer byteBuffer) {
    byteBuffer.put(bytes, offset, length);
  }

  @Override
  public MutableBytes mutableCopy() {
    return MutableBytes.fromArray(bytes, offset, length);
  }

  @Override
  public void appendTo(Buffer buffer) {
    buffer.appendBytes(bytes, offset, length);
  }

  @Override
  public byte[] toArrayUnsafe() {
    if (offset == 0 && length == bytes.length) {
      return bytes;
    }
    return Arrays.copyOfRange(bytes, offset, offset + length);
  }

  @Override
  protected void and(byte[] bytesArray, int offset, int length) {
    Utils.and(this.bytes, this.offset, bytesArray, offset, length);
  }

  @Override
  protected void or(byte[] bytesArray, int offset, int length) {
    Utils.or(this.bytes, this.offset, bytesArray, offset, length);
  }

  @Override
  protected void xor(byte[] bytesArray, int offset, int length) {
    Utils.xor(this.bytes, this.offset, bytesArray, offset, length);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Bytes other)) {
      return false;
    }

    if (this.size() != other.size()) {
      return false;
    }

    for (int i = 0; i < length; i++) {
      if (bytes[i + offset] != other.get(i)) {
        return false;
      }
    }

    return true;
  }
}

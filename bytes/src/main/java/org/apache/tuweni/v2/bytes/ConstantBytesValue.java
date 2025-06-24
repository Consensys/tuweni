// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.bytes;

import java.util.Arrays;

/**
 * A Bytes value with just one constant value throughout. Ideal to avoid allocating large byte
 * arrays filled with the same byte.
 */
class ConstantBytesValue extends Bytes {

  private final int size;
  private final byte value;

  ConstantBytesValue(byte b, int size) {
    this.value = b;
    this.size = size;
  }

  @Override
  public int size() {
    return this.size;
  }

  @Override
  public byte get(int i) {
    return this.value;
  }

  @Override
  public Bytes slice(int i, int length) {
    if (length == size) {
      return this;
    }
    return new ConstantBytesValue(this.value, length);
  }

  @Override
  protected void and(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      // TODO: Speed this up with SIMD
      bytesArray[offset + i] = (byte) (value & bytesArray[offset + i]);
    }
  }

  @Override
  protected void or(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      // TODO: Speed this up with SIMD
      bytesArray[offset + i] = (byte) (value | bytesArray[offset + i]);
    }
  }

  @Override
  protected void xor(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      // TODO: Speed this up with SIMD
      bytesArray[offset + i] = (byte) (value ^ bytesArray[offset + i]);
    }
  }

  @Override
  public MutableBytes mutableCopy() {
    MutableBytes mutableBytes = MutableBytes.create(size);
    mutableBytes.fill(value);
    return mutableBytes;
  }

  @Override
  public byte[] toArrayUnsafe() {
    byte[] array = new byte[size];
    Arrays.fill(array, value);
    return array;
  }
}

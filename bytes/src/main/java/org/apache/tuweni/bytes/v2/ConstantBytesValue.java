// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes.v2;

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
  void and(int offset, byte[] bytesArray) {
    for (int i = 0; i < size(); i++) {
      // TODO: Speed this up with SIMD
      bytesArray[offset + i] = (byte) (value & bytesArray[offset + i]);
    }
  }

  @Override
  void or(int offset, byte[] bytesArray) {
    for (int i = 0; i < size(); i++) {
      // TODO: Speed this up with SIMD
      bytesArray[offset + i] = (byte) (value | bytesArray[offset + i]);
    }
  }

  @Override
  void xor(int offset, byte[] bytesArray) {
    for (int i = 0; i < size(); i++) {
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
  byte[] toArrayUnsafe() {
    byte[] array = new byte[size];
    Arrays.fill(array, value);
    return array;
  }
}

// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes.v2;

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

//  TODO: Finish MutableBytes
//  @Override
//  public MutableBytes mutableCopy() {
//    byte[] mutable = new byte[this.size];
//    Arrays.fill(mutable, this.value);
//    return new MutableArrayWrappingBytes(mutable, 0, mutable.length);
//  }
}

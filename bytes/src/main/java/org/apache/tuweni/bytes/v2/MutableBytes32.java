// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes.v2;

import static org.apache.tuweni.bytes.v2.Bytes32.SIZE;
import static org.apache.tuweni.bytes.v2.Checks.checkArgument;
import static org.apache.tuweni.bytes.v2.Checks.checkNotNull;

/** A mutable {@link Bytes32}, that is a mutable {@link Bytes} value of exactly 32 bytes. */
public final class MutableBytes32 extends MutableBytes {

  private MutableBytes32(byte[] bytesArray) {
    super(bytesArray);
    checkArgument(bytesArray.length == SIZE, "Expected %s bytes but got %s", SIZE, bytesArray.length);
  }

  private MutableBytes32(byte[] bytesArray) {
    super(bytesArray);
    checkArgument(bytesArray.length == SIZE, "Expected %s bytes but got %s", SIZE, bytesArray.length);
  }

  /**
   * Create a new mutable 32 bytes value.
   *
   * @return A newly allocated {@link MutableBytes} value.
   */
  public static MutableBytes32 create() {
    return new MutableBytes32(new byte[SIZE]);
  }

  /**
   * Wrap a 32 bytes array as a mutable 32 bytes value.
   *
   * @param value The value to wrap.
   * @return A {@link MutableBytes32} wrapping {@code value}.
   * @throws IllegalArgumentException if {@code value.length != 32}.
   */
  public static MutableBytes32 wrap(byte[] value) {
    checkNotNull(value);
    return new MutableBytes32(value);
  }

  /**
   * Wrap the provided array as a {@link MutableBytes32}.
   *
   * <p>Note that value is not copied, only wrapped, and thus any future update to {@code value}
   * within the wrapped parts will be reflected in the returned value.
   *
   * @param value The bytes to wrap.
   * @param offset The index (inclusive) in {@code value} of the first byte exposed by the returned
   *     value. In other words, you will have {@code wrap(value, i).get(0) == value[i]}.
   * @return A {@link MutableBytes32} that exposes the bytes of {@code value} from {@code offset}
   *     (inclusive) to {@code offset + 32} (exclusive).
   * @throws IndexOutOfBoundsException if {@code offset < 0 || (value.length > 0 && offset >=
   *     value.length)}.
   * @throws IllegalArgumentException if {@code length < 0 || offset + 32 > value.length}.
   */
  public static MutableBytes32 wrap(byte[] value, int offset) {
    checkNotNull(value);
    return new MutableBytes32(value, offset, value.length);
  }

  /**
   * Wrap the provided value, which must be of size 32, as a {@link MutableBytes32}.
   *
   * <p>Note that value is not copied, only wrapped, and thus any future update to {@code value}
   * will be reflected in the returned value.
   *
   * @param value The bytes to wrap.
   * @return A {@link MutableBytes32} that exposes the bytes of {@code value}.
   * @throws IllegalArgumentException if {@code value.size() != 32}.
   */
  public static MutableBytes32 wrap(Bytes32 value) {
    checkNotNull(value);
    return DelegatingMutableBytes32.delegateTo(value);
  }

  /**
   * Wrap a slice/sub-part of the provided value as a {@link MutableBytes32}.
   *
   * <p>Note that the value is not copied, and thus any future update to {@code value} within the
   * wrapped parts will be reflected in the returned value.
   *
   * @param value The bytes to wrap.
   * @param offset The index (inclusive) in {@code value} of the first byte exposed by the returned
   *     value. In other words, you will have {@code wrap(value, i).get(0) == value.get(i)}.
   * @return A {@link Bytes32} that exposes the bytes of {@code value} from {@code offset}
   *     (inclusive) to {@code offset + 32} (exclusive).
   * @throws IndexOutOfBoundsException if {@code offset < 0 || (value.size() > 0 && offset >=
   *     value.size())}.
   * @throws IllegalArgumentException if {@code length < 0 || offset + 32 > value.size()}.
   */
  public static MutableBytes32 wrap(MutableBytes value, int offset) {
    checkNotNull(value);
    if (value instanceof MutableBytes32) {
      return (MutableBytes32) value;
    }
    MutableBytes slice = value.mutableSlice(offset, SIZE);
    if (slice instanceof MutableBytes32) {
      return (MutableBytes32) slice;
    }
    return DelegatingMutableBytes32.delegateTo(slice);
  }

  /**
   * Left pad a {@link Bytes} value with a fill byte to create a {@link Bytes32}.
   *
   * @param value The bytes value pad.
   * @param fill the byte to fill with
   * @return A {@link Bytes32} that exposes the left-padded bytes of {@code value}.
   * @throws IllegalArgumentException if {@code value.size() > 32}.
   */
  public static MutableBytes32 leftPad(MutableBytes value, byte fill) {
    checkNotNull(value);
    if (value instanceof MutableBytes32 bytes32) {
      return bytes32;
    }
    checkArgument(value.size() <= SIZE, "Expected at most %s bytes but got %s", SIZE, value.size());
    MutableBytes32 result = create();
    result.fill(fill);
    value.copyTo(result, SIZE - value.size());
    return result;
  }

  /**
   * Left pad a {@link Bytes} value with zero bytes to create a {@link Bytes32}.
   *
   * @param value The bytes value pad.
   * @return A {@link Bytes32} that exposes the left-padded bytes of {@code value}.
   * @throws IllegalArgumentException if {@code value.size() > 32}.
   */
  public static MutableBytes32 leftPad(MutableBytes value) {
    checkNotNull(value);
    if (value instanceof MutableBytes32 bytes32) {
      return bytes32;
    }
    checkArgument(value.size() <= SIZE, "Expected at most %s bytes but got %s", SIZE, value.size());
    MutableBytes32 result = create();
    value.copyTo(result, SIZE - value.size());
    return result;
  }

  /**
   * Right pad a {@link Bytes} value with zero bytes to create a {@link Bytes32}.
   *
   * @param value The bytes value pad.
   * @return A {@link Bytes32} that exposes the rightw-padded bytes of {@code value}.
   * @throws IllegalArgumentException if {@code value.size() > 32}.
   */
  public static MutableBytes32 rightPad(MutableBytes value) {
    checkNotNull(value);
    if (value instanceof MutableBytes32 bytes32) {
      return bytes32;
    }
    checkArgument(value.size() <= SIZE, "Expected at most %s bytes but got %s", SIZE, value.size());
    MutableBytes32 result = create();
    value.copyTo(result, 0);
    return result;
  }

  /**
   * Return a bit-wise AND of these bytes and the supplied bytes.
   *
   * @param other The bytes to perform the operation with.
   * @return The result of a bit-wise AND.
   */
  public MutableBytes32 and(MutableBytes32 other) {
    return and(other, create());
  }

  /**
   * Return a bit-wise OR of these bytes and the supplied bytes.
   *
   * @param other The bytes to perform the operation with.
   * @return The result of a bit-wise OR.
   */
  public MutableBytes32 or(MutableBytes32 other) {
    return or(other, create());
  }

  /**
   * Return a bit-wise XOR of these bytes and the supplied bytes.
   *
   * @param other The bytes to perform the operation with.
   * @return The result of a bit-wise XOR.
   */
  public MutableBytes32 xor(MutableBytes32 other) {
    return xor(other, create());
  }

  @Override
  public void not() {
    not(MutableBytes32.create());
  }

  @Override
  public void shiftRight(int distance) {
    shiftRight(distance, create());
  }

  @Override
  public void shiftLeft(int distance) {
    shiftLeft(distance, MutableBytes32.create());
  }

  @Override
  public MutableBytes mutableSlice(int i, int length) {
    return null;
  }

  @Override
  public int size() {
    return SIZE;
  }

  @Override
  public void set(int i, byte b) {

  }

  @Override
  public byte get(int i) {
    return 0;
  }

}

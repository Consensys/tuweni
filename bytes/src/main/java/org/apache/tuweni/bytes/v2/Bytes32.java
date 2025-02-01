// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes.v2;

import static org.apache.tuweni.bytes.v2.Checks.checkNotNull;

import java.security.SecureRandom;
import java.util.Random;

/** A {@link Bytes} value that is guaranteed to contain exactly 32 bytes. */
public final class Bytes32 extends DelegatingBytes {
  public static final int SIZE = 32;

  /** A {@code Bytes32} containing all zero bytes */
  public Bytes32 ZERO = Bytes32.repeat((byte) 0);

  private Bytes32(Bytes delegate) {
    super(delegate, SIZE);
  }

  /**
   * Generate a bytes object filled with the same byte.
   *
   * @param b the byte to fill the Bytes with
   * @return a value filled with a fixed byte
   */
  public static Bytes32 repeat(byte b) {
    return new Bytes32(new ConstantBytesValue(b, 32));
  }

  /**
   * Wrap the provided byte array, which must be of length 32, as a {@link Bytes32}.
   *
   * <p>Note that value is not copied, only wrapped, and thus any future update to {@code value}
   * will be reflected in the returned value.
   *
   * @param bytes The bytes to wrap.
   * @return A {@link Bytes32} wrapping {@code value}.
   * @throws IllegalArgumentException if {@code value.length != 32}.
   */
  public static Bytes32 wrap(byte[] bytes) {
    checkNotNull(bytes);
    return wrap(bytes, 0);
  }

  /**
   * Wrap a slice/sub-part of the provided array as a {@link Bytes32}.
   *
   * <p>Note that value is not copied, only wrapped, and thus any future update to {@code value}
   * within the wrapped parts will be reflected in the returned value.
   *
   * @param bytes The bytes to wrap.
   * @param offset The index (inclusive) in {@code value} of the first byte exposed by the returned
   *     value. In other words, you will have {@code wrap(value, i).get(0) == value[i]}.
   * @return A {@link Bytes32} that exposes the bytes of {@code value} from {@code offset}
   *     (inclusive) to {@code offset + 32} (exclusive).
   * @throws IndexOutOfBoundsException if {@code offset < 0 || (value.length > 0 && offset >=
   *     value.length)}.
   * @throws IllegalArgumentException if {@code length < 0 || offset + 32 > value.length}.
   */
  public static Bytes32 wrap(byte[] bytes, int offset) {
    checkNotNull(bytes);
    return new Bytes32(new ArrayWrappingBytes(bytes, offset, SIZE));
  }

  /**
   * Wrap a the provided value, which must be of size 32, as a {@link Bytes32}.
   *
   * <p>Note that value is not copied, only wrapped, and thus any future update to {@code value}
   * will be reflected in the returned value.
   *
   * @param value The bytes to wrap.
   * @return A {@link Bytes32} that exposes the bytes of {@code value}.
   * @throws IllegalArgumentException if {@code value.size() != 32}.
   */
  public static Bytes32 wrap(Bytes value) {
    checkNotNull(value);
    if (value instanceof Bytes32 bytes32) {
      return bytes32;
    }
    return new Bytes32(value.getImpl());
  }

  /**
   * Wrap a slice/sub-part of the provided value as a {@link Bytes32}.
   *
   * <p>Note that value is not copied, only wrapped, and thus any future update to {@code value}
   * within the wrapped parts will be reflected in the returned value.
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
  public static Bytes32 wrap(Bytes value, int offset) {
    checkNotNull(value);
    Bytes slice = value.slice(offset, SIZE);
    if (slice instanceof Bytes32 bytes32) {
      return bytes32;
    }
    return new Bytes32(slice.getImpl());
  }

  /**
   * Parse a hexadecimal string into a {@link Bytes32}.
   *
   * <p>This method is lenient in that {@code str} may of an odd length, in which case it will
   * behave exactly as if it had an additional 0 in front.
   *
   * @param str The hexadecimal string to parse, which may or may not start with "0x". That
   *     representation may contain less than 32 bytes, in which case the result is left padded with
   *     zeros (see {@link #fromHexStringStrict} if this is not what you want).
   * @return The value corresponding to {@code str}.
   * @throws IllegalArgumentException if {@code str} does not correspond to a valid hexadecimal
   *     representation or contains more than 32 bytes.
   */
  public static Bytes32 fromHexStringLenient(CharSequence str) {
    checkNotNull(str);
    return wrap(BytesValues.fromRawHexString(str, SIZE, true));
  }

  /**
   * Parse a hexadecimal string into a {@link Bytes32}.
   *
   * <p>This method is strict in that {@code str} must of an even length.
   *
   * @param str The hexadecimal string to parse, which may or may not start with "0x". That
   *     representation may contain less than 32 bytes, in which case the result is left padded with
   *     zeros (see {@link #fromHexStringStrict} if this is not what you want).
   * @return The value corresponding to {@code str}.
   * @throws IllegalArgumentException if {@code str} does not correspond to a valid hexadecimal
   *     representation, is of an odd length, or contains more than 32 bytes.
   */
  public static Bytes32 fromHexString(CharSequence str) {
    checkNotNull(str);
    return wrap(BytesValues.fromRawHexString(str, SIZE, false));
  }

  /**
   * Generate random bytes.
   *
   * @return A value containing random bytes.
   */
  public static Bytes32 random() {
    return random(new SecureRandom());
  }

  /**
   * Generate random bytes.
   *
   * @param generator The generator for random bytes.
   * @return A value containing random bytes.
   */
  public static Bytes32 random(Random generator) {
    byte[] array = new byte[32];
    generator.nextBytes(array);
    return wrap(array);
  }

  /**
   * Parse a hexadecimal string into a {@link Bytes32}.
   *
   * <p>This method is extra strict in that {@code str} must of an even length and the provided
   * representation must have exactly 32 bytes.
   *
   * @param str The hexadecimal string to parse, which may or may not start with "0x".
   * @return The value corresponding to {@code str}.
   * @throws IllegalArgumentException if {@code str} does not correspond to a valid hexadecimal
   *     representation, is of an odd length or does not contain exactly 32 bytes.
   */
  public static Bytes32 fromHexStringStrict(CharSequence str) {
    checkNotNull(str);
    return wrap(BytesValues.fromRawHexString(str, -1, false));
  }

//  TODO: Finish MutableBytes
//  MutableBytes32 mutableCopy() {
//    return MutableBytes32.wrap(delegate);
//  }
}

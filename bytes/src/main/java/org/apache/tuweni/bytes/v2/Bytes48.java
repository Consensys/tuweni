// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes.v2;

import static org.apache.tuweni.bytes.v2.Checks.checkNotNull;

import java.security.SecureRandom;
import java.util.Random;

/** A {@link Bytes} value that is guaranteed to contain exactly 48 bytes. */
public final class Bytes48 extends DelegatingBytes {
  /** The number of bytes in this value - i.e. 48 */
  public static final int SIZE = 48;

  /** A {@code Bytes48} containing all zero bytes */
  public static Bytes48 ZERO = repeat((byte) 0);

  private Bytes48(Bytes delegate) {
    super(delegate, SIZE);
  }

  /**
   * Generate a bytes object filled with the same byte.
   *
   * @param b the byte to fill the Bytes with
   * @return a value filled with a fixed byte
   */
  public static Bytes48 repeat(byte b) {
    return new Bytes48(new ConstantBytesValue(b, SIZE));
  }

  /**
   * Wrap the provided byte array, which must be of length 48, as a {@link Bytes48}.
   *
   * <p>Note that value is not copied, only wrapped, and thus any future update to {@code value}
   * will be reflected in the returned value.
   *
   * @param bytes The bytes to wrap.
   * @return A {@link Bytes48} wrapping {@code value}.
   * @throws IllegalArgumentException if {@code value.length != 48}.
   */
  public static Bytes48 wrap(byte[] bytes) {
    checkNotNull(bytes);
    return wrap(bytes, 0);
  }

  /**
   * Wrap a slice/sub-part of the provided array as a {@link Bytes48}.
   *
   * <p>Note that value is not copied, only wrapped, and thus any future update to {@code value}
   * within the wrapped parts will be reflected in the returned value.
   *
   * @param bytes The bytes to wrap.
   * @param offset The index (inclusive) in {@code value} of the first byte exposed by the returned
   *     value. In other words, you will have {@code wrap(value, i).get(0) == value[i]}.
   * @return A {@link Bytes48} that exposes the bytes of {@code value} from {@code offset}
   *     (inclusive) to {@code offset + 48} (exclusive).
   * @throws IndexOutOfBoundsException if {@code offset < 0 || (value.length > 0 && offset >=
   *     value.length)}.
   * @throws IllegalArgumentException if {@code length < 0 || offset + 48 > value.length}.
   */
  public static Bytes48 wrap(byte[] bytes, int offset) {
    checkNotNull(bytes);
    return new Bytes48(new ArrayWrappingBytes(bytes, offset, SIZE));
  }

  /**
   * Wrap a the provided value, which must be of size 48, as a {@link Bytes48}.
   *
   * <p>Note that value is not copied, only wrapped, and thus any future update to {@code value}
   * will be reflected in the returned value.
   *
   * @param value The bytes to wrap.
   * @return A {@link Bytes48} that exposes the bytes of {@code value}.
   * @throws IllegalArgumentException if {@code value.size() != 48}.
   */
  public static Bytes48 wrap(Bytes value) {
    checkNotNull(value);
    if (value instanceof Bytes48 bytes48) {
      return bytes48;
    }
    return new Bytes48(value.getImpl());
  }

  /**
   * Wrap a slice/sub-part of the provided value as a {@link Bytes48}.
   *
   * <p>Note that value is not copied, only wrapped, and thus any future update to {@code value}
   * within the wrapped parts will be reflected in the returned value.
   *
   * @param value The bytes to wrap.
   * @param offset The index (inclusive) in {@code value} of the first byte exposed by the returned
   *     value. In other words, you will have {@code wrap(value, i).get(0) == value.get(i)}.
   * @return A {@link Bytes48} that exposes the bytes of {@code value} from {@code offset}
   *     (inclusive) to {@code offset + 48} (exclusive).
   * @throws IndexOutOfBoundsException if {@code offset < 0 || (value.size() > 0 && offset >=
   *     value.size())}.
   * @throws IllegalArgumentException if {@code length < 0 || offset + 48 > value.size()}.
   */
  public static Bytes48 wrap(Bytes value, int offset) {
    checkNotNull(value);
    Bytes slice = value.slice(offset, SIZE);
    if (slice instanceof Bytes48 bytes48) {
      return bytes48;
    }
    return new Bytes48(value.getImpl());
  }

  /**
   * Parse a hexadecimal string into a {@link Bytes48}.
   *
   * <p>This method is lenient in that {@code str} may of an odd length, in which case it will
   * behave exactly as if it had an additional 0 in front.
   *
   * @param str The hexadecimal string to parse, which may or may not start with "0x". That
   *     representation may contain less than 48 bytes, in which case the result is left padded with
   *     zeros (see {@link #fromHexStringStrict} if this is not what you want).
   * @return The value corresponding to {@code str}.
   * @throws IllegalArgumentException if {@code str} does not correspond to a valid hexadecimal
   *     representation or contains more than 48 bytes.
   */
  public static Bytes48 fromHexStringLenient(CharSequence str) {
    checkNotNull(str);
    return wrap(BytesValues.fromRawHexString(str, SIZE, true));
  }

  /**
   * Parse a hexadecimal string into a {@link Bytes48}.
   *
   * <p>This method is strict in that {@code str} must of an even length.
   *
   * @param str The hexadecimal string to parse, which may or may not start with "0x". That
   *     representation may contain less than 48 bytes, in which case the result is left padded with
   *     zeros (see {@link #fromHexStringStrict} if this is not what you want).
   * @return The value corresponding to {@code str}.
   * @throws IllegalArgumentException if {@code str} does not correspond to a valid hexadecimal
   *     representation, is of an odd length, or contains more than 48 bytes.
   */
  public static Bytes48 fromHexString(CharSequence str) {
    checkNotNull(str);
    return wrap(BytesValues.fromRawHexString(str, SIZE, false));
  }

  /**
   * Generate random bytes.
   *
   * @return A value containing random bytes.
   */
  public static Bytes48 random() {
    return random(new SecureRandom());
  }

  /**
   * Generate random bytes.
   *
   * @param generator The generator for random bytes.
   * @return A value containing random bytes.
   */
  public static Bytes48 random(Random generator) {
    byte[] array = new byte[48];
    generator.nextBytes(array);
    return wrap(array);
  }

  /**
   * Parse a hexadecimal string into a {@link Bytes48}.
   *
   * <p>This method is extra strict in that {@code str} must of an even length and the provided
   * representation must have exactly 48 bytes.
   *
   * @param str The hexadecimal string to parse, which may or may not start with "0x".
   * @return The value corresponding to {@code str}.
   * @throws IllegalArgumentException if {@code str} does not correspond to a valid hexadecimal
   *     representation, is of an odd length or does not contain exactly 48 bytes.
   */
  public static Bytes48 fromHexStringStrict(CharSequence str) {
    checkNotNull(str);
    return wrap(BytesValues.fromRawHexString(str, -1, false));
  }

//  TODO: Finish MutableBytes
//  MutableBytes48 mutableCopy() {
//    return MutableBytes48.wrap(delegate);
//  }
}

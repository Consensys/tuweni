// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.units.bigints;

import static org.apache.tuweni.bytes.v2.Utils.checkElementIndex;

import org.apache.tuweni.bytes.v2.Bytes;
import org.apache.tuweni.bytes.v2.MutableBytes;

import java.math.BigInteger;
import java.nio.ByteOrder;

/**
 * An unsigned 32-bit precision number.
 *
 * <p>This is a raw 32-bit precision unsigned number of no particular unit.
 */
public final class UInt32 extends Bytes {
  private static final int MAX_CONSTANT = 0xff;
  private static UInt32[] CONSTANTS = new UInt32[MAX_CONSTANT + 1];

  static {
    CONSTANTS[0] = new UInt32(0);
    for (int i = 1; i <= MAX_CONSTANT; ++i) {
      CONSTANTS[i] = new UInt32(i);
    }
  }

  /** The minimum value of a UInt32 */
  public static final UInt32 MIN_VALUE = valueOf(0);

  /** The maximum value of a UInt32 */
  public static final UInt32 MAX_VALUE = create(~0);

  /** The value 0 */
  public static final UInt32 ZERO = valueOf(0);

  /** The value 1 */
  public static final UInt32 ONE = valueOf(1);

  private static final BigInteger P_2_32 = BigInteger.valueOf(2).pow(32);

  private final int value;

  /**
   * Return a {@code UInt32} containing the specified value.
   *
   * @param value The value to create a {@code UInt32} for.
   * @return A {@code UInt32} containing the specified value.
   * @throws IllegalArgumentException If the value is negative.
   */
  public static UInt32 valueOf(int value) {
    if (value < 0) {
      throw new IllegalArgumentException("Argument must be positive");
    }
    return create(value);
  }

  /**
   * Return a {@link UInt32} containing the specified value.
   *
   * @param value the value to create a {@link UInt32} for
   * @return a {@link UInt32} containing the specified value
   * @throws IllegalArgumentException if the value is negative or too large to be represented as a
   *     UInt32
   */
  public static UInt32 valueOf(BigInteger value) {
    if (value.bitLength() > 32) {
      throw new IllegalArgumentException("Argument is too large to represent a UInt32");
    }
    if (value.signum() < 0) {
      throw new IllegalArgumentException("Argument must be positive");
    }
    return create(value.intValue());
  }

  /**
   * Return a {@link UInt32} containing the value described by the specified bytes.
   *
   * @param bytes The bytes containing a {@link UInt32}. \ * @return A {@link UInt32} containing the
   *     specified value.
   * @throws IllegalArgumentException if {@code bytes.size() > 4}.
   */
  public static UInt32 fromBytes(Bytes bytes) {
    return fromBytes(bytes, ByteOrder.BIG_ENDIAN);
  }

  /**
   * Return a {@link UInt32} containing the value described by the specified bytes.
   *
   * @param bytes The bytes containing a {@link UInt32}.
   * @param byteOrder the byte order of the value
   * @return A {@link UInt32} containing the specified value.
   * @throws IllegalArgumentException if {@code bytes.size() > 4}.
   */
  public static UInt32 fromBytes(Bytes bytes, ByteOrder byteOrder) {
    if (bytes.size() > 4) {
      throw new IllegalArgumentException("Argument is greater than 4 bytes");
    }
    return create(byteOrder == ByteOrder.LITTLE_ENDIAN ? bytes.mutableCopy().reverse() : bytes);
  }

  /**
   * Parse a hexadecimal string into a {@link UInt32}.
   *
   * @param str The hexadecimal string to parse, which may or may not start with "0x". That
   *     representation may contain less than 8 bytes, in which case the result is left padded with
   *     zeros.
   * @return The value corresponding to {@code str}.
   * @throws IllegalArgumentException if {@code str} does not correspond to a valid hexadecimal
   *     representation or contains more than 8 bytes.
   */
  public static UInt32 fromHexString(String str) {
    return fromBytes(Bytes.fromHexStringLenient(str));
  }

  private static UInt32 create(Bytes value) {
    return create(value.toInt());
  }

  private static UInt32 create(int value) {
    if (value >= 0 && value <= MAX_CONSTANT) {
      return CONSTANTS[value];
    }
    return new UInt32(value);
  }

  private UInt32(int value) {
    this.value = value;
  }

  @Override
  public boolean isZero() {
    return ZERO.equals(this);
  }

  public UInt32 add(UInt32 value) {
    if (value.isZero()) {
      return this;
    }
    return create(this.value + value.value);
  }

  public UInt32 add(int value) {
    if (value == 0) {
      return this;
    }
    return create(this.value + value);
  }

  public UInt32 addMod(UInt32 value, UInt32 modulus) {
    if (modulus.isZero()) {
      throw new ArithmeticException("addMod with zero modulus");
    }
    return create(toBigInteger().add(value.toBigInteger()).mod(modulus.toBigInteger()).intValue());
  }

  public UInt32 addMod(long value, UInt32 modulus) {
    if (modulus.isZero()) {
      throw new ArithmeticException("addMod with zero modulus");
    }
    return create(
        toBigInteger().add(BigInteger.valueOf(value)).mod(modulus.toBigInteger()).intValue());
  }

  public UInt32 addMod(long value, long modulus) {
    if (modulus == 0) {
      throw new ArithmeticException("addMod with zero modulus");
    }
    if (modulus < 0) {
      throw new ArithmeticException("addMod unsigned with negative modulus");
    }
    return create(
        toBigInteger().add(BigInteger.valueOf(value)).mod(BigInteger.valueOf(modulus)).intValue());
  }

  public UInt32 subtract(UInt32 value) {
    if (value.isZero()) {
      return this;
    }

    return create(this.value - value.value);
  }

  public UInt32 subtract(int value) {
    if (value == 0) {
      return this;
    }
    return create(this.value - value);
  }

  public UInt32 multiply(UInt32 value) {
    return create(this.value * value.value);
  }

  public UInt32 multiply(int value) {
    if (value < 0) {
      throw new ArithmeticException("multiply unsigned by negative");
    }
    if (value == 0 || isZero()) {
      return ZERO;
    }
    return multiply(UInt32.valueOf(value));
  }

  public UInt32 multiplyMod(UInt32 value, UInt32 modulus) {
    if (modulus.isZero()) {
      throw new ArithmeticException("multiplyMod with zero modulus");
    }
    if (isZero() || value.isZero()) {
      return ZERO;
    }
    if (ONE.equals(value)) {
      return mod(modulus);
    }
    return create(
        toBigInteger().multiply(value.toBigInteger()).mod(modulus.toBigInteger()).intValue());
  }

  public UInt32 multiplyMod(int value, UInt32 modulus) {
    if (modulus.isZero()) {
      throw new ArithmeticException("multiplyMod with zero modulus");
    }
    if (value == 0 || this.isZero()) {
      return ZERO;
    }
    if (value == 1) {
      return mod(modulus);
    }
    if (value < 0) {
      throw new ArithmeticException("multiplyMod unsigned by negative");
    }
    return create(
        toBigInteger().multiply(BigInteger.valueOf(value)).mod(modulus.toBigInteger()).intValue());
  }

  public UInt32 multiplyMod(int value, int modulus) {
    if (modulus == 0) {
      throw new ArithmeticException("multiplyMod with zero modulus");
    }
    if (modulus < 0) {
      throw new ArithmeticException("multiplyMod unsigned with negative modulus");
    }
    if (value == 0 || this.isZero()) {
      return ZERO;
    }
    if (value == 1) {
      return mod(modulus);
    }
    if (value < 0) {
      throw new ArithmeticException("multiplyMod unsigned by negative");
    }
    return create(
        toBigInteger()
            .multiply(BigInteger.valueOf(value))
            .mod(BigInteger.valueOf(modulus))
            .intValue());
  }

  public UInt32 divide(UInt32 value) {
    if (value.isZero()) {
      throw new ArithmeticException("divide by zero");
    }

    if (value.equals(ONE)) {
      return this;
    }
    return create(toBigInteger().divide(value.toBigInteger()).intValue());
  }

  public UInt32 divide(int value) {
    if (value == 0) {
      throw new ArithmeticException("divide by zero");
    }
    if (value < 0) {
      throw new ArithmeticException("divide unsigned by negative");
    }
    if (value == 1) {
      return this;
    }
    if (isPowerOf2(value)) {
      return fromBytes(mutableCopy().shiftRight(log2(value)));
    }
    return create(toBigInteger().divide(BigInteger.valueOf(value)).intValue());
  }

  public UInt32 pow(UInt32 exponent) {
    return create(toBigInteger().modPow(exponent.toBigInteger(), P_2_32).intValue());
  }

  public UInt32 pow(long exponent) {
    return create(toBigInteger().modPow(BigInteger.valueOf(exponent), P_2_32).intValue());
  }

  public UInt32 mod(UInt32 modulus) {
    if (modulus.isZero()) {
      throw new ArithmeticException("mod by zero");
    }
    return create(Integer.remainderUnsigned(this.value, modulus.value));
  }

  public UInt32 mod(int modulus) {
    if (modulus == 0) {
      throw new ArithmeticException("mod by zero");
    }
    if (modulus < 0) {
      throw new ArithmeticException("mod by negative");
    }
    return create(Integer.remainderUnsigned(this.value, modulus));
  }

  @Override
  public boolean equals(Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof UInt32 other)) {
      return false;
    }
    return this.value == other.value;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(this.value);
  }

  public int compareTo(UInt32 other) {
    return Long.compareUnsigned(this.value, other.value);
  }

  @Override
  protected void and(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      bytesArray[offset + i] = (byte) (get(i) & bytesArray[offset + i]);
    }
  }

  @Override
  protected void or(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      bytesArray[offset + i] = (byte) (get(i) | bytesArray[offset + i]);
    }
  }

  @Override
  protected void xor(byte[] bytesArray, int offset, int length) {
    for (int i = 0; i < length; i++) {
      bytesArray[offset + i] = (byte) (get(i) ^ bytesArray[offset + i]);
    }
  }

  @Override
  public int size() {
    return 4;
  }

  @Override
  public byte get(int i) {
    checkElementIndex(i, size());
    return Utils.unpackByte(value, i);
  }

  @Override
  public BigInteger toBigInteger() {
    byte[] mag = new byte[4];
    mag[0] = (byte) (this.value >>> 24);
    mag[1] = (byte) (this.value >>> 16);
    mag[2] = (byte) (this.value >>> 8);
    mag[3] = (byte) this.value;
    return new BigInteger(1, mag);
  }

  public UInt32 toUInt32() {
    return this;
  }

  public Bytes toBytes() {
    return Bytes.wrap(toArrayUnsafe());
  }

  public Bytes toMinimalBytes() {
    int numberOfLeadingZeroBytes = Integer.numberOfLeadingZeros(this.value) / 8;
    return slice(numberOfLeadingZeroBytes);
  }

  @Override
  public int bitLength() {
    return 32 - numberOfLeadingZeros();
  }

  @Override
  public Bytes slice(int i, int length) {
    return toBytes().slice(i, length);
  }

  @Override
  public MutableBytes mutableCopy() {
    return MutableBytes.fromArray(toArrayUnsafe());
  }

  @Override
  public byte[] toArrayUnsafe() {
    return new byte[] {
      (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value
    };
  }

  private static boolean isPowerOf2(long n) {
    assert n > 0;
    return (n & (n - 1)) == 0;
  }

  private static int log2(int v) {
    assert v > 0;
    return 63 - Long.numberOfLeadingZeros(v);
  }

  /**
   * Returns a value that is {@code (this + value)}.
   *
   * @param value the amount to be added to this value
   * @return {@code this + value}
   * @throws ArithmeticException if the result of the addition overflows
   */
  UInt32 addExact(UInt32 value) {
    UInt32 result = add(value);
    if (compareTo(result) > 0) {
      throw new ArithmeticException("UInt32 overflow");
    }
    return result;
  }

  /**
   * Returns a value that is {@code (this + value)}.
   *
   * @param value the amount to be added to this value
   * @return {@code this + value}
   * @throws ArithmeticException if the result of the addition overflows
   */
  UInt32 addExact(int value) {
    UInt32 result = add(value);
    if ((value > 0 && compareTo(result) > 0) || (value < 0 && compareTo(result) < 0)) {
      throw new ArithmeticException("UInt32 overflow");
    }
    return result;
  }

  /**
   * Returns a value that is {@code (this - value)}.
   *
   * @param value the amount to be subtracted to this value
   * @return {@code this - value}
   * @throws ArithmeticException if the result of the subtraction overflows
   */
  public UInt32 subtractExact(UInt32 value) {
    UInt32 result = subtract(value);
    if (compareTo(result) < 0) {
      throw new ArithmeticException("UInt32 overflow");
    }
    return result;
  }

  /**
   * Returns a value that is {@code (this - value)}.
   *
   * @param value the amount to be subtracted to this value
   * @return {@code this - value}
   * @throws ArithmeticException if the result of the subtraction overflows
   */
  public UInt32 subtractExact(int value) {
    UInt32 result = subtract(value);
    if ((value > 0 && compareTo(result) < 0) || (value < 0 && compareTo(result) > 0)) {
      throw new ArithmeticException("UInt32 overflow");
    }
    return result;
  }

  /**
   * Returns the decimal representation of this value as a String.
   *
   * @return the decimal representation of this value as a String.
   */
  public String toDecimalString() {
    return toBigInteger().toString(10);
  }
}

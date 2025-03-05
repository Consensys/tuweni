// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes.v2;

import static java.lang.String.format;
import static org.apache.tuweni.bytes.v2.Utils.checkArgument;
import static org.apache.tuweni.bytes.v2.Utils.checkElementIndex;
import static org.apache.tuweni.bytes.v2.Utils.checkNotNull;

import java.nio.ByteBuffer;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;

/** A class for doing modifications on a {@link Bytes} value without modifying the original. */
public class MutableBytes {

  private byte[] bytesArray;

  // Use for slicing and avoiding creating a new array
  private int offset;
  private int length;

  MutableBytes(int size) {
    this.offset = 0;
    this.length = size;
    this.bytesArray = new byte[length];
  }

  MutableBytes(byte[] bytesArray) {
    this.offset = 0;
    this.length = bytesArray.length;
    this.bytesArray = new byte[length];
    System.arraycopy(bytesArray, offset, this.bytesArray, 0, length);
  }

  MutableBytes(byte[] bytesArray, int offset, int length) {
    this.offset = 0;
    this.length = length;
    this.bytesArray = new byte[length];
    System.arraycopy(bytesArray, offset, this.bytesArray, 0, length);
  }

  /**
   * Create a new mutable bytes value.
   *
   * @param size The size of the returned value.
   * @return A {@link MutableBytes} value.
   */
  public static MutableBytes create(int size) {
    return new MutableBytes(size);
  }

  /**
   * Create a {@link MutableBytes} value from a byte array.
   *
   * @param value The value to wrap.
   * @return A {@link MutableBytes} value wrapping {@code value}.
   */
  public static MutableBytes fromArray(byte[] value) {
    checkNotNull(value);
    if (value.length == 0) {
      return MutableBytes.create(0);
    }
    return new MutableBytes(value);
  }

  /**
   * Wrap a slice of a byte array as a {@link MutableBytes} value.
   *
   * <p>Note that value is not copied and thus any future update to {@code value} within the slice
   * will be reflected in the returned value.
   *
   * @param value The value to wrap.
   * @param offset The index (inclusive) in {@code value} of the first byte exposed by the returned
   *     value. In other words, you will have {@code wrap(value, o, l).get(0) == value[o]}.
   * @param length The length of the resulting value.
   * @return A {@link Bytes} value that expose the bytes of {@code value} from {@code offset}
   *     (inclusive) to {@code offset + length} (exclusive).
   * @throws IndexOutOfBoundsException if {@code offset < 0 || (value.length > 0 && offset >=
   *     value.length)}.
   * @throws IllegalArgumentException if {@code length < 0 || offset + length > value.length}.
   */
  public static MutableBytes fromArray(byte[] value, int offset, int length) {
    checkNotNull(value);
    checkArgument(length >= 0, "Invalid negative length");
    if (value.length > 0) {
      checkElementIndex(offset, value.length);
    }
    checkArgument(
        offset + length <= value.length,
        "Provided length %s is too big: the value has only %s bytes from offset %s",
        length,
        value.length - offset,
        offset);
    if (length == 0) {
      return MutableBytes.create(0);
    }
    return new MutableBytes(value, offset, length);
  }

  /**
   * Wrap a full Vert.x {@link Buffer} as a {@link MutableBytes} value.
   *
   * <p>Note that any change to the content of the buffer may be reflected in the returned value.
   *
   * @param buffer The buffer to wrap.
   * @return A {@link MutableBytes} value.
   */
  public static MutableBytes fromBuffer(Buffer buffer) {
    checkNotNull(buffer);
    if (buffer.length() == 0) {
      return MutableBytes.create(0);
    }
    return new MutableBytes(buffer.getBytes());
  }

  /**
   * Wrap a slice of a Vert.x {@link Buffer} as a {@link MutableBytes} value.
   *
   * <p>Note that any change to the content of the buffer may be reflected in the returned value,
   * and any change to the returned value will be reflected in the buffer.
   *
   * @param buffer The buffer to wrap.
   * @param offset The offset in {@code buffer} from which to expose the bytes in the returned
   *     value. That is, {@code wrapBuffer(buffer, i, 1).get(0) == buffer.getByte(i)}.
   * @param length The size of the returned value.
   * @return A {@link MutableBytes} value.
   * @throws IndexOutOfBoundsException if {@code offset < 0 || (buffer.length() > 0 && offset >=
   *     buffer.length())}.
   * @throws IllegalArgumentException if {@code length < 0 || offset + length > buffer.length()}.
   */
  public static MutableBytes fromBuffer(Buffer buffer, int offset, int length) {
    checkNotNull(buffer);
    checkArgument(length >= 0, "Invalid negative length");
    if (buffer.length() > 0) {
      checkElementIndex(offset, buffer.length());
    }
    checkArgument(
        offset + length <= buffer.length(),
        "Provided length %s is too big: the value has only %s bytes from offset %s",
        length,
        buffer.length() - offset,
        offset);
    if (length == 0) {
      return MutableBytes.create(0);
    }
    return new MutableBytes(buffer.getBytes(), offset, length);
  }

  /**
   * Wrap a full Netty {@link ByteBuf} as a {@link MutableBytes} value.
   *
   * <p>Note that any change to the content of the buffer may be reflected in the returned value.
   *
   * @param byteBuf The {@link ByteBuf} to wrap.
   * @return A {@link MutableBytes} value.
   */
  public static MutableBytes fromByteBuf(ByteBuf byteBuf) {
    checkNotNull(byteBuf);
    if (byteBuf.capacity() == 0) {
      return MutableBytes.create(0);
    }
    MutableBytes mutableBytes = MutableBytes.create(byteBuf.capacity());
    byteBuf.getBytes(0, mutableBytes.bytesArray);
    return mutableBytes;
  }

  /**
   * Wrap a slice of a Netty {@link ByteBuf} as a {@link MutableBytes} value.
   *
   * <p>Note that any change to the content of the buffer may be reflected in the returned value,
   * and any change to the returned value will be reflected in the buffer.
   *
   * @param byteBuf The {@link ByteBuf} to wrap.
   * @param offset The offset in {@code byteBuf} from which to expose the bytes in the returned
   *     value. That is, {@code wrapByteBuf(byteBuf, i, 1).get(0) == byteBuf.getByte(i)}.
   * @param length The size of the returned value.
   * @return A {@link MutableBytes} value.
   * @throws IndexOutOfBoundsException if {@code offset < 0 || (byteBuf.capacity() > 0 && offset >=
   *     byteBuf.capacity())}.
   * @throws IllegalArgumentException if {@code length < 0 || offset + length > byteBuf.capacity()}.
   */
  public static MutableBytes fromByteBuf(ByteBuf byteBuf, int offset, int length) {
    checkNotNull(byteBuf);
    checkArgument(length >= 0, "Invalid negative length");
    if (byteBuf.capacity() > 0) {
      checkElementIndex(offset, byteBuf.capacity());
    }
    checkArgument(
        offset + length <= byteBuf.capacity(),
        "Provided length %s is too big: the value has only %s bytes from offset %s",
        length,
        byteBuf.capacity() - offset,
        offset);
    if (length == 0) {
      return MutableBytes.create(0);
    }
    MutableBytes mutableBytes = MutableBytes.create(length);
    byteBuf.getBytes(offset, mutableBytes.bytesArray);
    return mutableBytes;
  }

  /**
   * Wrap a full Java NIO {@link ByteBuffer} as a {@link MutableBytes} value.
   *
   * <p>Note that any change to the content of the buffer may be reflected in the returned value.
   *
   * @param byteBuffer The {@link ByteBuffer} to wrap.
   * @return A {@link MutableBytes} value.
   */
  public static MutableBytes fromByteBuffer(ByteBuffer byteBuffer) {
    checkNotNull(byteBuffer);
    if (byteBuffer.limit() == 0) {
      return MutableBytes.create(0);
    }
    MutableBytes mutableBytes = MutableBytes.create(byteBuffer.limit());
    byteBuffer.get(0, mutableBytes.bytesArray);
    return mutableBytes;
  }

  /**
   * Wrap a slice of a Java NIO {@link ByteBuffer} as a {@link MutableBytes} value.
   *
   * <p>Note that any change to the content of the buffer may be reflected in the returned value,
   * and any change to the returned value will be reflected in the buffer.
   *
   * @param byteBuffer The {@link ByteBuffer} to wrap.
   * @param offset The offset in {@code byteBuffer} from which to expose the bytes in the returned
   *     value. That is, {@code wrapByteBuffer(byteBuffer, i, 1).get(0) == byteBuffer.getByte(i)}.
   * @param length The size of the returned value.
   * @return A {@link MutableBytes} value.
   * @throws IndexOutOfBoundsException if {@code offset < 0 || (byteBuffer.limit() > 0 && offset >=
   *     byteBuffer.limit())}.
   * @throws IllegalArgumentException if {@code length < 0 || offset + length > byteBuffer.limit()}.
   */
  public static MutableBytes fromByteBuffer(ByteBuffer byteBuffer, int offset, int length) {
    checkNotNull(byteBuffer);
    checkArgument(length >= 0, "Invalid negative length");
    if (byteBuffer.limit() > 0) {
      checkElementIndex(offset, byteBuffer.limit());
    }
    checkArgument(
        offset + length <= byteBuffer.limit(),
        "Provided length %s is too big: the value has only %s bytes from offset %s",
        length,
        byteBuffer.limit() - offset,
        offset);
    if (length == 0) {
      return MutableBytes.create(0);
    }
    MutableBytes mutableBytes = MutableBytes.create(length);
    byteBuffer.get(offset, mutableBytes.bytesArray);
    return mutableBytes;
  }

  /**
   * Create a value that contains the specified bytes in their specified order.
   *
   * @param bytes The bytes that must compose the returned value.
   * @return A value containing the specified bytes.
   */
  public static MutableBytes of(byte... bytes) {
    return fromArray(bytes);
  }

  /**
   * Create a value that contains the specified bytes in their specified order.
   *
   * @param bytes The bytes.
   * @return A value containing bytes are the one from {@code bytes}.
   * @throws IllegalArgumentException if any of the specified would be truncated when storing as a
   *     byte.
   */
  public static MutableBytes of(int... bytes) {
    checkNotNull(bytes);
    byte[] result = new byte[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      int b = bytes[i];
      checkArgument(b == (((byte) b) & 0xff), "%sth value %s does not fit a byte", i + 1, b);
      result[i] = (byte) b;
    }
    return fromArray(result);
  }

  /**
   * Set a byte in this value.
   *
   * @param index The offset of the bytes to set.
   * @param bytes The value to set bytes to.
   * @throws IndexOutOfBoundsException if {@code offset < 0} or {offset >= size()}.
   * @throws IllegalArgumentException if {@code offset + bytes.size() > this.length}.
   */
  public void set(int index, Bytes bytes) {
    checkNotNull(bytes);
    checkElementIndex(index, length);
    checkArgument(
        index + bytes.size() <= length,
        "Provided length %s is too big: the value has only %s bytes from offset %s",
        bytes.size(),
        length - index,
        index);
    for (int i = 0; i < bytes.size(); i++) {
      set(i + index + offset, bytes.get(i));
    }
  }

  /**
   * Set a byte array in this value.
   *
   * @param index The offset of the bytes to set.
   * @param bytes The value to set bytes to.
   * @throws IndexOutOfBoundsException if {@code offset < 0} or {offset >= bytes.length}.
   * @throws IllegalArgumentException if {@code offset + bytes.length > this.length}.
   */
  public void set(int index, byte[] bytes) {
    checkNotNull(bytes);
    checkElementIndex(index, length);
    checkArgument(
        index + bytes.length <= length,
        "Provided length %s is too big: the value has only %s bytes from offset %s",
        bytes.length,
        length - index,
        index);
    for (int i = 0; i < bytes.length; i++) {
      set(i + index + offset, bytes[i]);
    }
  }

  /**
   * Set the 4 bytes starting at the specified index to the specified integer value.
   *
   * @param index The index, which must less than or equal to {@code size() - 4}.
   * @param value The integer value.
   * @throws IndexOutOfBoundsException if {@code index < 0} or {@code index > size() - 4}.
   */
  public void setInt(int index, int value) {
    checkElementIndex(index, length);
    if (index > (length - 4)) {
      throw new IndexOutOfBoundsException(
          format(
              "Value of size %s has not enough bytes to write a 4 bytes int from index %s",
              length, index));
    }

    index += offset;
    set(index++, (byte) (value >>> 24));
    set(index++, (byte) ((value >>> 16) & 0xFF));
    set(index++, (byte) ((value >>> 8) & 0xFF));
    set(index, (byte) (value & 0xFF));
  }

  /**
   * Set the 8 bytes starting at the specified index to the specified long value.
   *
   * @param index The index, which must less than or equal to {@code size() - 8}.
   * @param value The long value.
   * @throws IndexOutOfBoundsException if {@code index < 0} or {@code index > size() - 8}.
   */
  public void setLong(int index, long value) {
    checkElementIndex(index, length);
    if (index > (length - 8)) {
      throw new IndexOutOfBoundsException(
          format(
              "Value of length %s has not enough bytes to write a 8 bytes long from index %s",
              length, index));
    }

    index += offset;
    set(index++, (byte) (value >>> 56));
    set(index++, (byte) ((value >>> 48) & 0xFF));
    set(index++, (byte) ((value >>> 40) & 0xFF));
    set(index++, (byte) ((value >>> 32) & 0xFF));
    set(index++, (byte) ((value >>> 24) & 0xFF));
    set(index++, (byte) ((value >>> 16) & 0xFF));
    set(index++, (byte) ((value >>> 8) & 0xFF));
    set(index, (byte) (value & 0xFF));
  }

  /**
   * Increments the value of the bytes by 1, treating the value as big endian.
   *
   * <p>If incrementing overflows the value then all bits flip, i.e. incrementing 0xFFFF will return
   * 0x0000.
   *
   * <p>This is less efficient than {@code MutableBytes.increment()} as it does not create new
   * objects for computation and should be preferred when more than one computation is done in
   * sequence.
   *
   * @param b value to increment.
   * @return a new instance of Bytes that stores the new value incremented.
   */
  public static Bytes increment(Bytes b) {
    MutableBytes mutableBytes = b.mutableCopy();
    mutableBytes.increment();
    return new ArrayWrappingBytes(mutableBytes.bytesArray);
  }

  /**
   * Increments the value of the bytes by 1, treating the value as big endian.
   *
   * <p>If incrementing overflows the value then all bits flip, i.e. incrementing 0xFFFF will return
   * 0x0000.
   *
   * @return This mutable bytes instance.
   */
  public MutableBytes increment() {
    for (int i = offset + length - 1; i >= offset; --i) {
      if (bytesArray[i] == (byte) 0xFF) {
        bytesArray[i] = (byte) 0x00;
      } else {
        byte currentValue = bytesArray[i];
        bytesArray[i] = ++currentValue;
        break;
      }
    }
    return this;
  }

  /**
   * Decrements the value of the bytes by 1, treating the value as big endian.
   *
   * <p>If decrementing overflows the value then all bits flip, i.e. decrementing 0xFFFF will return
   * 0x0000.
   *
   * <p>This is less efficient than {@code MutableBytes.decrement()} as it does not create new
   * objects for computation and should be preferred when more than one computation is done in
   * sequence.
   *
   * @param b value to decrement.
   * @return a new instance of Bytes that stores the new value decremented.
   */
  public static Bytes decrement(Bytes b) {
    MutableBytes mutableBytes = b.mutableCopy();
    mutableBytes.decrement();
    return new ArrayWrappingBytes(mutableBytes.bytesArray);
  }

  /**
   * Decrements the value of the bytes by 1, treating the value as big endian.
   *
   * <p>If decrementing underflows the value then all bits flip, i.e. decrementing 0x0000 will
   * return 0xFFFF.
   *
   * @return This mutable bytes instance.
   */
  public MutableBytes decrement() {
    for (int i = offset + length - 1; i >= offset; --i) {
      if (bytesArray[i] == (byte) 0x00) {
        bytesArray[i] = (byte) 0xFF;
      } else {
        byte currentValue = bytesArray[i];
        bytesArray[i] = --currentValue;
        break;
      }
    }
    return this;
  }

  /**
   * Fill all the bytes of this value with the specified byte.
   *
   * <p>This is less efficient than {@code MutableBytes.fill(byte b)} as it does not create new
   * objects for computation and should be preferred when more than one computation is done in
   * sequence.
   *
   * @param bytes value to fill with the byte.
   * @param b value used to fill the Bytes value.
   * @return a new instance of Bytes that stores the new value.
   */
  public static Bytes fill(Bytes bytes, byte b) {
    MutableBytes mutableBytes = bytes.mutableCopy();
    mutableBytes.fill(b);
    return new ArrayWrappingBytes(mutableBytes.bytesArray);
  }

  /**
   * Fill all the bytes of this value with the specified byte.
   *
   * @param b The byte to use to fill the value.
   * @return This mutable bytes instance.
   */
  public MutableBytes fill(byte b) {
    for (int i = offset; i < offset + length; i++) {
      bytesArray[i] = b;
    }
    return this;
  }

  /**
   * Set all bytes in this value to 0.
   *
   * <p>This is less efficient than {@code MutableBytes.clear()} as it does not create new objects
   * for computation and should be preferred when more than one computation is done in sequence.
   *
   * @param b Bytes value to be cleared.
   * @return a new instance of Bytes that stores the new value.
   */
  public static Bytes clear(Bytes b) {
    MutableBytes mutableBytes = b.mutableCopy();
    mutableBytes.clear();
    return new ArrayWrappingBytes(mutableBytes.bytesArray);
  }

  /**
   * Set all bytes in this value to 0.
   *
   * @return This mutable bytes instance.
   */
  public MutableBytes clear() {
    fill((byte) 0);
    return this;
  }

  /**
   * Set all bytes in this value to 0.
   *
   * <p>This is less efficient than {@code MutableBytes.revert()} as it does not create new objects
   * for computation and should be preferred when more than one computation is done in sequence.
   *
   * @param b Bytes value to be reverted.
   * @return a new instance of Bytes that stores the new value.
   */
  public static Bytes reverse(Bytes b) {
    MutableBytes mutableBytes = b.mutableCopy();
    mutableBytes.reverse();
    return new ArrayWrappingBytes(mutableBytes.bytesArray);
  }

  /**
   * Computes the reverse array of bytes of the current bytes.
   *
   * @return This mutable bytes instance.
   */
  public MutableBytes reverse() {
    byte[] reverse = new byte[length];
    for (int i = 0; i < length; i++) {
      reverse[length - 1 - i] = bytesArray[i + offset];
    }
    bytesArray = reverse;
    offset = 0;
    return this;
  }

  /**
   * Calculate a bit-wise AND between two Bytes values and return the result.
   *
   * <p>This is less efficient than {@code MutableBytes.or(Bytes b)} as it does not create new
   * objects for computation and should be preferred when more than one computation is done in
   * sequence.
   *
   * @param b1 first operand of the compute.
   * @param b2 second operand of the compute.
   * @return a new instance of Bytes that stores the result of the compute.
   */
  public static Bytes and(Bytes b1, Bytes b2) {
    MutableBytes resultBytes = b1.mutableCopy();
    resultBytes.and(b2);
    return new ArrayWrappingBytes(resultBytes.bytesArray);
  }

  /**
   * Calculate a bit-wise AND of these bytes and the supplied bytes.
   *
   * @param other The bytes to perform the operation with.
   * @throws IllegalArgumentException if sizes of both bytes mismatch.
   * @return This mutable bytes instance.
   */
  public MutableBytes and(Bytes other) {
    checkNotNull(other);
    checkArgument(other.size() == length, "size %s does not match size %s", other.size(), length);
    other.and(bytesArray, offset, length);
    return this;
  }

  /**
   * Calculate a bit-wise OR between two Bytes values and return the result.
   *
   * <p>This is less efficient than {@code MutableBytes.or(Bytes b)} as it does not create new
   * objects for computation and should be preferred when more than one computation is done in
   * sequence.
   *
   * @param b1 first operand of the compute.
   * @param b2 second operand of the compute.
   * @return a new instance of Bytes that stores the result of the compute.
   */
  public static Bytes or(Bytes b1, Bytes b2) {
    MutableBytes resultBytes = b1.mutableCopy();
    resultBytes.or(b2);
    return new ArrayWrappingBytes(resultBytes.bytesArray);
  }

  /**
   * Calculate a bit-wise OR of these bytes and the supplied bytes.
   *
   * @param other The bytes to perform the operation with.
   * @throws IllegalArgumentException if sizes of both bytes mismatch.
   * @return This mutable bytes instance.
   */
  public MutableBytes or(Bytes other) {
    checkNotNull(other);
    checkArgument(other.size() == length, "size %s does not match size %s", other.size(), length);
    other.or(bytesArray, offset, length);
    return this;
  }

  /**
   * Calculate a bit-wise XOR between two Bytes values and return the result.
   *
   * <p>This is less efficient than {@code MutableBytes.xor(Bytes b)} as it does not create new
   * objects for computation and should be preferred when more than one computation is done in
   * sequence.
   *
   * @param b1 first operand of the compute.
   * @param b2 second operand of the compute.
   * @return a new instance of Bytes that stores the result of the compute.
   */
  public static Bytes xor(Bytes b1, Bytes b2) {
    MutableBytes resultBytes = b1.mutableCopy();
    resultBytes.xor(b2);
    return new ArrayWrappingBytes(resultBytes.bytesArray, resultBytes.offset, resultBytes.length);
  }

  /**
   * Calculate a bit-wise XOR of these bytes and the supplied bytes.
   *
   * @param other The bytes to perform the operation with.
   * @throws IllegalArgumentException if sizes of both bytes mismatch.
   * @return This mutable bytes instance.
   */
  public MutableBytes xor(Bytes other) {
    checkNotNull(other);
    checkArgument(other.size() == length, "size %s does not match size %s", other.size(), length);
    other.xor(bytesArray, offset, length);
    return this;
  }

  /**
   * Calculate a bit-wise NOT between two Bytes values and return the result.
   *
   * <p>This is less efficient than {@code MutableBytes.or(Bytes b)} as it does not create new
   * objects for computation and should be preferred when more than one computation is done in
   * sequence.
   *
   * @param b Bytes value to compute the not operation.
   * @return a new instance of Bytes that stores the result of the compute.
   */
  public static Bytes not(Bytes b) {
    MutableBytes resultBytes = b.mutableCopy();
    resultBytes.not();
    return new ArrayWrappingBytes(resultBytes.bytesArray);
  }

  /**
   * Calculate a bit-wise NOT of these bytes.
   *
   * @return This mutable bytes instance.
   */
  public MutableBytes not() {
    for (int i = offset; i < offset + length; i++) {
      bytesArray[i] = (byte) ~bytesArray[i];
    }
    return this;
  }

  /**
   * Shift all bits of a given Bytes value to the right by {@code distance} bits.
   *
   * <p>This is less efficient than {@code MutableBytes.shiftRight(int distance)} as it does not
   * create new objects for computation and should be preferred when more than one computation is
   * done in sequence.
   *
   * @param b Bytes value to shift bytes.
   * @return a new instance of Bytes that stores the result of the compute.
   */
  public static Bytes shiftRight(Bytes b, int distance) {
    MutableBytes resultBytes = b.mutableCopy();
    resultBytes.shiftRight(distance);
    return new ArrayWrappingBytes(resultBytes.bytesArray);
  }

  /**
   * Shift all bits in this value to the right.
   *
   * @param distance The number of bits to shift by.
   * @return This mutable bytes instance.
   */
  public MutableBytes shiftRight(int distance) {
    checkArgument(distance >= 0, "Invalid negative distance");
    if (distance == 0) {
      return this;
    }
    distance = Math.min(distance, length * 8);
    int byteShift = distance / 8;
    int bitShift = distance % 8;

    if (byteShift > 0) {
      for (int i = offset + length - 1; i >= offset; i--) {
        byte previousByte = (i < byteShift) ? 0 : bytesArray[i - byteShift];
        bytesArray[i] = previousByte;
      }
    }

    if (bitShift > 0) {
      for (int i = offset + length - 1; i >= offset; i--) {
        byte currentByte = bytesArray[i];
        byte previousByte = (i == 0) ? 0 : bytesArray[i - 1];
        int rightSide = (currentByte & 0XFF) >>> bitShift;
        int leftSide = previousByte << (8 - bitShift);
        bytesArray[i] = (byte) (leftSide | rightSide);
      }
    }
    return this;
  }

  /**
   * Shift all bits of a given Bytes value to the left by {@code distance} bits.
   *
   * <p>This is less efficient than {@code MutableBytes.shiftLeft(int distance)} as it does not
   * create new objects for computation and should be preferred when more than one computation is
   * done in sequence.
   *
   * @param b Bytes value to shift bytes.
   * @return a new instance of Bytes that stores the result of the compute.
   */
  public static Bytes shiftLeft(Bytes b, int distance) {
    MutableBytes resultBytes = b.mutableCopy();
    resultBytes.shiftLeft(distance);
    return new ArrayWrappingBytes(resultBytes.bytesArray);
  }

  /**
   * Shift all bits in this value to the left.
   *
   * @param distance The number of bits to shift by.
   * @return This mutable bytes instance.
   */
  public MutableBytes shiftLeft(int distance) {
    checkArgument(distance >= 0, "Invalid negative distance");
    if (distance == 0) {
      return this;
    }
    distance = Math.min(distance, length * 8);
    int byteShift = distance / 8;
    int bitShift = distance % 8;

    if (byteShift > 0) {
      for (int i = offset; i < offset + length; i++) {
        byte nextByte = (i + byteShift < offset + length) ? bytesArray[i + byteShift] : 0;
        bytesArray[i] = nextByte;
      }
    }

    if (bitShift > 0) {
      for (int i = offset; i < offset + length; i++) {
        byte currentByte = bytesArray[i];
        byte nextByte = (i == offset + length - 1) ? 0 : bytesArray[i + 1];
        int leftSide = currentByte << bitShift;
        int rightSide = (nextByte & 0XFF) >>> (8 - bitShift);
        bytesArray[i] = (byte) (leftSide | rightSide);
      }
    }
    return this;
  }

  /**
   * Left pad some Bytes value with zero bytes up to the specified size. Resulting Bytes are
   * guaranteed to have at least {@code size} bytes in length but not necessarily that exact amount.
   * If length already exceeds {@code size} then bytes are not modified.
   *
   * <p>This is less efficient than {@code MutableBytes.leftPad(int size)} as it does not create new
   * objects for computation and should be preferred when more than one computation is done in
   * sequence.
   *
   * @param b Bytes value to pad.
   * @return a new instance of Bytes that stores the result of the compute.
   */
  public static Bytes leftPad(Bytes b, int size) {
    MutableBytes resultBytes = b.mutableCopy();
    resultBytes.leftPad(size);
    return new ArrayWrappingBytes(resultBytes.bytesArray);
  }

  /**
   * Left pad these mutable values with zero bytes up to the specified size. Resulting bytes are
   * guaranteed to have at least {@code size} bytes in length but not necessarily that exact amount.
   * If length already exceeds {@code size} then bytes are not modified.
   *
   * @param size The new size of the bytes.
   * @throws IllegalArgumentException if {@code size} is negative.
   * @return This mutable bytes instance.
   */
  public MutableBytes leftPad(int size) {
    checkArgument(size >= 0, "Invalid negative size");
    if (size <= length) {
      return this;
    }
    byte[] newBytesArray = new byte[size];
    System.arraycopy(bytesArray, offset, newBytesArray, size - length, length);
    bytesArray = newBytesArray;
    offset = 0;
    length = size;
    return this;
  }

  /**
   * Right pad some Bytes value with zero bytes up to the specified size. Resulting Bytes are
   * guaranteed to have at least {@code size} bytes in length but not necessarily that exact amount.
   * If length already exceeds {@code size} then bytes are not modified.
   *
   * <p>This is less efficient than {@code MutableBytes.rightPad(int size)} as it does not create
   * new objects for computation and should be preferred when more than one computation is done in
   * sequence.
   *
   * @param b Bytes value to pad.
   * @return a new instance of Bytes that stores the result of the compute.
   */
  public static Bytes rightPad(Bytes b, int size) {
    MutableBytes resultBytes = b.mutableCopy();
    resultBytes.rightPad(size);
    return new ArrayWrappingBytes(resultBytes.bytesArray);
  }

  /**
   * Right pad these mutable values with zero bytes up to the specified size. Resulting bytes are
   * guaranteed to have at least {@code size} bytes in length but not necessarily that exact amount.
   * If length already exceeds {@code size} then bytes are not modified.
   *
   * @param size The new size of the bytes.
   * @throws IllegalArgumentException if {@code size} is negative.
   * @return This mutable bytes instance.
   */
  public MutableBytes rightPad(int size) {
    checkArgument(size >= 0, "Invalid negative size");
    if (size <= length) {
      return this;
    }
    byte[] newBytesArray = new byte[size];
    System.arraycopy(bytesArray, offset, newBytesArray, 0, length);
    bytesArray = newBytesArray;
    offset = 0;
    length = size;
    return this;
  }

  public MutableBytes slice(int offset, int length) {
    checkArgument(length >= 0, "Invalid negative length");
    if (this.length > 0) {
      checkElementIndex(offset, this.length);
    }
    checkArgument(
        offset + length <= this.length,
        "Provided length %s is too big: the value has only %s bytes from offset %s",
        length,
        this.length - offset,
        offset);
    if (length == this.length) {
      return this;
    }
    return new MutableBytes(this.bytesArray, offset, length);
  }

  /**
   * Provides the number of bytes this value represents.
   *
   * @return The number of bytes this value represents.
   */
  public int size() {
    return length;
  }

  /**
   * Set a byte in this value.
   *
   * @param i The index of the byte to set.
   * @param b The value to set that byte to.
   * @throws IndexOutOfBoundsException if {@code i < 0} or {i >= size()}.
   */
  public void set(int i, byte b) {
    bytesArray[offset + i] = b;
  }

  /**
   * Gets a byte at the given index.
   *
   * @param i The index of the byte to get.
   * @throws IndexOutOfBoundsException if {@code i < 0} or {i >= size()}.
   * @return The byte at the given index.
   */
  public byte get(int i) {
    return bytesArray[offset + i];
  }

  @Override
  public int hashCode() {
    int result = 1;
    for (int i = offset; i < length; i++) {
      result = 31 * result + bytesArray[i];
    }
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof MutableBytes other)) {
      return false;
    }

    if (this.length != other.length) {
      return false;
    }

    for (int i = 0; i < length; i++) {
      if (bytesArray[i + offset] != other.bytesArray[i + offset]) {
        return false;
      }
    }

    return true;
  }

  /**
   * Extract the bytes of this value into a byte array.
   *
   * @return A byte array with the same content than this value.
   */
  public byte[] toArray() {
    return bytesArray;
  }

  /**
   * Turn these mutable bytes into an immutable reference.
   *
   * @return an instance of Bytes
   */
  public Bytes toBytes() {
    return new ArrayWrappingBytes(bytesArray, offset, length);
  }

  /**
   * Parse a hexadecimal string into a {@link MutableBytes} value.
   *
   * <p>This method requires that {@code str} have an even length.
   *
   * @param str The hexadecimal string to parse, which may or may not start with "0x".
   * @return The value corresponding to {@code str}.
   * @throws IllegalArgumentException if {@code str} does not correspond to a valid hexadecimal
   *     representation, or is of an odd length.
   */
  public static MutableBytes fromHexString(CharSequence str) {
    checkNotNull(str);
    return MutableBytes.fromArray(BytesValues.fromRawHexString(str, -1, false));
  }
}

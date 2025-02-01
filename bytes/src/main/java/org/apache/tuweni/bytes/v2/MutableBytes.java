// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.bytes.v2;

import static java.lang.String.format;
import static org.apache.tuweni.bytes.v2.Checks.checkArgument;
import static org.apache.tuweni.bytes.v2.Checks.checkElementIndex;
import static org.apache.tuweni.bytes.v2.Checks.checkNotNull;

import java.nio.ByteBuffer;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;

/** A class for doing modifications on a {@link Bytes} value without modifying the original. */
public class MutableBytes {
  static final MutableBytes EMPTY = create(0);

  private final MutableArrayWrappingBytes mutableBytes;

  MutableBytes(byte[] bytesArray) {
    this(bytesArray, 0, bytesArray.length);
  }

  MutableBytes(byte[] bytesArray, int offset, int length) {
    this.mutableBytes = new MutableArrayWrappingBytes(bytesArray, offset, length);
  }

  /**
   * Create a new mutable bytes value.
   *
   * @param size The size of the returned value.
   * @return A {@link MutableBytes} value.
   */
  public static MutableBytes create(int size) {
    return new MutableBytes(new byte[size]);
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
      return EMPTY;
    }
    byte[] newValue = new byte[value.length];
    System.arraycopy(value, 0, newValue, 0, value.length);
    return new MutableBytes(newValue);
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
  public static MutableBytes from(byte[] value, int offset, int length) {
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
    byte[] newValue = new byte[length - offset];
    return new MutableBytes(newValue);
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
      return EMPTY;
    }
    byte[] value = buffer.getBytes();
    return new MutableBytes(value);
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
   * @param size The size of the returned value.
   * @return A {@link MutableBytes} value.
   * @throws IndexOutOfBoundsException if {@code offset < 0 || (buffer.length() > 0 && offset >=
   *     buffer.length())}.
   * @throws IllegalArgumentException if {@code length < 0 || offset + length > buffer.length()}.
   */
  public static MutableBytes wrapBuffer(Buffer buffer, int offset, int size) {
    checkNotNull(buffer);
    if (size == 0) {
      return EMPTY;
    }
    byte[] value = buffer.getBytes();
    return new MutableBytes(buffer, offset, size);
  }

  /**
   * Wrap a full Netty {@link ByteBuf} as a {@link MutableBytes} value.
   *
   * <p>Note that any change to the content of the buffer may be reflected in the returned value.
   *
   * @param byteBuf The {@link ByteBuf} to wrap.
   * @return A {@link MutableBytes} value.
   */
  public static MutableBytes wrapByteBuf(ByteBuf byteBuf) {
    checkNotNull(byteBuf);
    if (byteBuf.capacity() == 0) {
      return EMPTY;
    }
    return new MutableByteBufWrappingBytes(byteBuf);
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
   * @param size The size of the returned value.
   * @return A {@link MutableBytes} value.
   * @throws IndexOutOfBoundsException if {@code offset < 0 || (byteBuf.capacity() > 0 && offset >=
   *     byteBuf.capacity())}.
   * @throws IllegalArgumentException if {@code length < 0 || offset + length > byteBuf.capacity()}.
   */
  public static MutableBytes wrapByteBuf(ByteBuf byteBuf, int offset, int size) {
    checkNotNull(byteBuf);
    if (size == 0) {
      return EMPTY;
    }
    return new MutableByteBufWrappingBytes(byteBuf, offset, size);
  }

  /**
   * Wrap a full Java NIO {@link ByteBuffer} as a {@link MutableBytes} value.
   *
   * <p>Note that any change to the content of the buffer may be reflected in the returned value.
   *
   * @param byteBuffer The {@link ByteBuffer} to wrap.
   * @return A {@link MutableBytes} value.
   */
  public static MutableBytes wrapByteBuffer(ByteBuffer byteBuffer) {
    checkNotNull(byteBuffer);
    if (byteBuffer.limit() == 0) {
      return EMPTY;
    }
    return new MutableByteBufferWrappingBytes(byteBuffer);
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
   * @param size The size of the returned value.
   * @return A {@link MutableBytes} value.
   * @throws IndexOutOfBoundsException if {@code offset < 0 || (byteBuffer.limit() > 0 && offset >=
   *     byteBuffer.limit())}.
   * @throws IllegalArgumentException if {@code length < 0 || offset + length > byteBuffer.limit()}.
   */
  public static MutableBytes wrapByteBuffer(ByteBuffer byteBuffer, int offset, int size) {
    checkNotNull(byteBuffer);
    if (size == 0) {
      return EMPTY;
    }
    return new MutableByteBufferWrappingBytes(byteBuffer, offset, size);
  }

  /**
   * Create a value that contains the specified bytes in their specified order.
   *
   * @param bytes The bytes that must compose the returned value.
   * @return A value containing the specified bytes.
   */
  public static MutableBytes of(byte... bytes) {
    return wrap(bytes);
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
    byte[] result = new byte[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      int b = bytes[i];
      checkArgument(b == (((byte) b) & 0xff), "%sth value %s does not fit a byte", i + 1, b);
      result[i] = (byte) b;
    }
    return wrap(result);
  }

  /**
   * Set a byte in this value.
   *
   * @param offset The offset of the bytes to set.
   * @param bytes The value to set bytes to.
   * @throws IndexOutOfBoundsException if {@code i < 0} or {i >= size()}.
   */
  public void set(int offset, Bytes bytes) {
    for (int i = 0; i < bytes.size(); i++) {
      set(offset + i, bytes.get(i));
    }
  }

  /**
   * Set the 4 bytes starting at the specified index to the specified integer value.
   *
   * @param i The index, which must less than or equal to {@code size() - 4}.
   * @param value The integer value.
   * @throws IndexOutOfBoundsException if {@code i < 0} or {@code i > size() - 4}.
   */
  public void setInt(int i, int value) {
    int size = size();
    checkElementIndex(i, size);
    if (i > (size - 4)) {
      throw new IndexOutOfBoundsException(
          format(
              "Value of size %s has not enough bytes to write a 4 bytes int from index %s",
              size, i));
    }

    set(i++, (byte) (value >>> 24));
    set(i++, (byte) ((value >>> 16) & 0xFF));
    set(i++, (byte) ((value >>> 8) & 0xFF));
    set(i, (byte) (value & 0xFF));
  }

  /**
   * Set the 8 bytes starting at the specified index to the specified long value.
   *
   * @param i The index, which must less than or equal to {@code size() - 8}.
   * @param value The long value.
   * @throws IndexOutOfBoundsException if {@code i < 0} or {@code i > size() - 8}.
   */
  public void setLong(int i, long value) {
    int size = size();
    checkElementIndex(i, size);
    if (i > (size - 8)) {
      throw new IndexOutOfBoundsException(
          format(
              "Value of size %s has not enough bytes to write a 8 bytes long from index %s",
              size, i));
    }

    set(i++, (byte) (value >>> 56));
    set(i++, (byte) ((value >>> 48) & 0xFF));
    set(i++, (byte) ((value >>> 40) & 0xFF));
    set(i++, (byte) ((value >>> 32) & 0xFF));
    set(i++, (byte) ((value >>> 24) & 0xFF));
    set(i++, (byte) ((value >>> 16) & 0xFF));
    set(i++, (byte) ((value >>> 8) & 0xFF));
    set(i, (byte) (value & 0xFF));
  }

  /**
   * Increments the value of the bytes by 1, treating the value as big endian.
   *
   * <p>If incrementing overflows the value then all bits flip, i.e. incrementing 0xFFFF will return
   * 0x0000.
   *
   * @return this value
   */
  public MutableBytes increment() {
    for (int i = size() - 1; i >= 0; --i) {
      if (get(i) == (byte) 0xFF) {
        set(i, (byte) 0x00);
      } else {
        byte currentValue = get(i);
        set(i, ++currentValue);
        break;
      }
    }
    return this;
  }

  /**
   * Decrements the value of the bytes by 1, treating the value as big endian.
   *
   * <p>If decrementing underflows the value then all bits flip, i.e. decrementing 0x0000 will
   * return 0xFFFF.
   *
   * @return this value
   */
  public MutableBytes decrement() {
    for (int i = size() - 1; i >= 0; --i) {
      if (get(i) == (byte) 0x00) {
        set(i, (byte) 0xFF);
      } else {
        byte currentValue = get(i);
        set(i, --currentValue);
        break;
      }
    }
    return this;
  }

  /**
   * Fill all the bytes of this value with the specified byte.
   *
   * @param b The byte to use to fill the value.
   */
  public void fill(byte b) {
    int size = size();
    for (int i = 0; i < size; i++) {
      set(i, b);
    }
  }

  /** Set all bytes in this value to 0. */
  public void clear() {
    fill((byte) 0);
  }

  /**
   * Return a bit-wise AND of these bytes and the supplied bytes.
   *
   * <p>If this value and the supplied value are different lengths, then the shorter will be
   * zero-padded to the left.
   *
   * @param other The bytes to perform the operation with.
   * @return The result of a bit-wise AND.
   */
  public void and(Bytes other) {
    and(other, MutableBytes.create(Math.max(size(), other.size())));
  }

  /**
   * Calculate a bit-wise AND of these bytes and the supplied bytes.
   *
   * <p>If this value or the supplied value are shorter in length than the output vector, then they
   * will be zero-padded to the left. Likewise, if either this value or the supplied valid is longer
   * in length than the output vector, then they will be truncated to the left.
   *
   * @param other The bytes to perform the operation with.
   * @param result The mutable output vector for the result.
   * @param <T> The {@link MutableBytes} value type.
   * @return The {@code result} output vector.
   */
  public <T extends MutableBytes> T and(Bytes other, T result) {
    checkNotNull(other);
    checkNotNull(result);
    int rSize = result.size();
    int offsetSelf = rSize - size();
    int offsetOther = rSize - other.size();
    for (int i = 0; i < rSize; i++) {
      byte b1 = (i < offsetSelf) ? 0x00 : get(i - offsetSelf);
      byte b2 = (i < offsetOther) ? 0x00 : other.get(i - offsetOther);
      result.set(i, (byte) (b1 & b2));
    }
    return result;
  }

  /**
   * Return a bit-wise OR of these bytes and the supplied bytes.
   *
   * <p>If this value and the supplied value are different lengths, then the shorter will be
   * zero-padded to the left.
   *
   * @param other The bytes to perform the operation with.
   * @return The result of a bit-wise OR.
   */
  public void or(Bytes other) {
    or(other, MutableBytes.create(Math.max(size(), other.size())));
  }

  /**
   * Calculate a bit-wise OR of these bytes and the supplied bytes.
   *
   * <p>If this value or the supplied value are shorter in length than the output vector, then they
   * will be zero-padded to the left. Likewise, if either this value or the supplied valid is longer
   * in length than the output vector, then they will be truncated to the left.
   *
   * @param other The bytes to perform the operation with.
   * @param result The mutable output vector for the result.
   * @param <T> The {@link MutableBytes} value type.
   * @return The {@code result} output vector.
   */
  public <T extends MutableBytes> T or(Bytes other, T result) {
    checkNotNull(other);
    checkNotNull(result);
    int rSize = result.size();
    int offsetSelf = rSize - size();
    int offsetOther = rSize - other.size();
    for (int i = 0; i < rSize; i++) {
      byte b1 = (i < offsetSelf) ? 0x00 : get(i - offsetSelf);
      byte b2 = (i < offsetOther) ? 0x00 : other.get(i - offsetOther);
      result.set(i, (byte) (b1 | b2));
    }
    return result;
  }

  /**
   * Return a bit-wise XOR of these bytes and the supplied bytes.
   *
   * <p>If this value and the supplied value are different lengths, then the shorter will be
   * zero-padded to the left.
   *
   * @param other The bytes to perform the operation with.
   * @return The result of a bit-wise XOR.
   */
  public void xor(Bytes other) {
    xor(other, MutableBytes.create(Math.max(size(), other.size())));
  }

  /**
   * Calculate a bit-wise XOR of these bytes and the supplied bytes.
   *
   * <p>If this value or the supplied value are shorter in length than the output vector, then they
   * will be zero-padded to the left. Likewise, if either this value or the supplied valid is longer
   * in length than the output vector, then they will be truncated to the left.
   *
   * @param other The bytes to perform the operation with.
   * @param result The mutable output vector for the result.
   * @param <T> The {@link MutableBytes} value type.
   * @return The {@code result} output vector.
   */
  public <T extends MutableBytes> T xor(Bytes other, T result) {
    checkNotNull(other);
    checkNotNull(result);
    int rSize = result.size();
    int offsetSelf = rSize - size();
    int offsetOther = rSize - other.size();
    for (int i = 0; i < rSize; i++) {
      byte b1 = (i < offsetSelf) ? 0x00 : get(i - offsetSelf);
      byte b2 = (i < offsetOther) ? 0x00 : other.get(i - offsetOther);
      result.set(i, (byte) (b1 ^ b2));
    }
    return result;
  }

  /**
   * Makes a bit-wise NOT of these bytes.
   */
  public void not() {
    not(MutableBytes.create(size()));
  }

  /**
   * Calculate a bit-wise NOT of these bytes.
   *
   * <p>If this value is shorter in length than the output vector, then it will be zero-padded to
   * the left. Likewise, if this value is longer in length than the output vector, then it will be
   * truncated to the left.
   *
   * @param result The mutable output vector for the result.
   * @param <T> The {@link MutableBytes} value type.
   * @return The {@code result} output vector.
   */
  public <T extends MutableBytes> T not(T result) {
    checkNotNull(result);
    int rSize = result.size();
    int offsetSelf = rSize - size();
    for (int i = 0; i < rSize; i++) {
      byte b1 = (i < offsetSelf) ? 0x00 : get(i - offsetSelf);
      result.set(i, (byte) ~b1);
    }
    return result;
  }

  /**
   * Shift all bits in this value to the right.
   *
   * @param distance The number of bits to shift by.
   * @return A value containing the shifted bits.
   */
  public void shiftRight(int distance) {
    shiftRight(distance, MutableBytes.create(size()));
  }

  /**
   * Shift all bits in this value to the right.
   *
   * <p>If this value is shorter in length than the output vector, then it will be zero-padded to
   * the left. Likewise, if this value is longer in length than the output vector, then it will be
   * truncated to the left (after shifting).
   *
   * @param distance The number of bits to shift by.
   * @param result The mutable output vector for the result.
   * @param <T> The {@link MutableBytes} value type.
   * @return The {@code result} output vector.
   */
  public <T extends MutableBytes> T shiftRight(int distance, T result) {
    checkNotNull(result);
    int rSize = result.size();
    int offsetSelf = rSize - size();

    int d = distance / 8;
    int s = distance % 8;
    int resIdx = rSize - 1;
    for (int i = rSize - 1 - d; i >= 0; i--) {
      byte res;
      if (i < offsetSelf) {
        res = 0;
      } else {
        int selfIdx = i - offsetSelf;
        int leftSide = (get(selfIdx) & 0xFF) >>> s;
        int rightSide = (selfIdx == 0) ? 0 : get(selfIdx - 1) << (8 - s);
        res = (byte) (leftSide | rightSide);
      }
      result.set(resIdx--, res);
    }
    for (; resIdx >= 0; resIdx--) {
      result.set(resIdx, (byte) 0);
    }
    return result;
  }

  /**
   * Shift all bits in this value to the left.
   *
   * @param distance The number of bits to shift by.
   * @return A value containing the shifted bits.
   */
  public void shiftLeft(int distance) {
    shiftLeft(distance, MutableBytes.create(size()));
  }

  /**
   * Shift all bits in this value to the left.
   *
   * <p>If this value is shorter in length than the output vector, then it will be zero-padded to
   * the left. Likewise, if this value is longer in length than the output vector, then it will be
   * truncated to the left.
   *
   * @param distance The number of bits to shift by.
   * @param result The mutable output vector for the result.
   * @param <T> The {@link MutableBytes} value type.
   * @return The {@code result} output vector.
   */
  public <T extends MutableBytes> T shiftLeft(int distance, T result) {
    checkNotNull(result);
    int size = size();
    int rSize = result.size();
    int offsetSelf = rSize - size;

    int d = distance / 8;
    int s = distance % 8;
    int resIdx = 0;
    for (int i = d; i < rSize; i++) {
      byte res;
      if (i < offsetSelf) {
        res = 0;
      } else {
        int selfIdx = i - offsetSelf;
        int leftSide = get(selfIdx) << s;
        int rightSide = (selfIdx == size - 1) ? 0 : (get(selfIdx + 1) & 0xFF) >>> (8 - s);
        res = (byte) (leftSide | rightSide);
      }
      result.set(resIdx++, res);
    }
    for (; resIdx < rSize; resIdx++) {
      result.set(resIdx, (byte) 0);
    }
    return result;
  }

  /**
   * Create a mutable slice of the bytes of this value.
   *
   * <p>Note: the resulting slice is only a view over the original value. Holding a reference to the
   * returned slice may hold more memory than the slide represents.
   *
   * @param i The start index for the slice.
   * @param length The length of the resulting value.
   * @return A new mutable view over the bytes of this value from index {@code i} (included) to
   *     index {@code i + length} (excluded).
   * @throws IllegalArgumentException if {@code length < 0}.
   * @throws IndexOutOfBoundsException if {@code i < 0} or {i >= size()} or {i + length > size()} .
   */
  public abstract void mutableSlice(int i, int length);

  /**
   * Provides the number of bytes this value represents.
   *
   * @return The number of bytes this value represents.
   */
  public abstract int size();

  /**
   * Set a byte in this value.
   *
   * @param i The index of the byte to set.
   * @param b The value to set that byte to.
   * @throws IndexOutOfBoundsException if {@code i < 0} or {i >= size()}.
   */
  public abstract void set(int i, byte b);

  /**
   * Retrieve a byte in this value.
   *
   * @param i The index of the byte to fetch within the value (0-indexed).
   * @return The byte at index {@code i} in this value.
   * @throws IndexOutOfBoundsException if {@code i < 0} or {i >= size()}.
   */
  public abstract byte get(int i);

  public abstract Bytes toBytesView();
}

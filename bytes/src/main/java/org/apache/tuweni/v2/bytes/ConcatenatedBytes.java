// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.v2.bytes;

import static org.apache.tuweni.v2.bytes.Utils.checkArgument;
import static org.apache.tuweni.v2.bytes.Utils.checkElementIndex;

import java.security.MessageDigest;
import java.util.List;

final class ConcatenatedBytes extends Bytes {

  private final Bytes[] values;
  private final int size;

  private ConcatenatedBytes(Bytes[] values, int totalSize) {
    this.values = values;
    this.size = totalSize;
  }

  static Bytes create(Bytes... values) {
    if (values.length == 0) {
      return EMPTY;
    }
    if (values.length == 1) {
      return values[0];
    }

    int count = 0;
    int totalSize = 0;

    for (Bytes value : values) {
      int size = value == null ? 0 : value.size();
      try {
        totalSize = Math.addExact(totalSize, size);
      } catch (ArithmeticException e) {
        throw new IllegalArgumentException(
            "Combined length of values is too long (> Integer.MAX_VALUE)");
      }
      if (value instanceof ConcatenatedBytes concatenatedBytes) {
        count += concatenatedBytes.values.length;
      } else if (size != 0) {
        count += 1;
      }
    }

    if (count == 0) {
      return EMPTY;
    }
    if (count == values.length) {
      return new ConcatenatedBytes(values, totalSize);
    }

    Bytes[] concatenated = new Bytes[count];
    int i = 0;
    for (Bytes value : values) {
      if (value instanceof ConcatenatedBytes concatenatedBytes) {
        Bytes[] subvalues = concatenatedBytes.values;
        System.arraycopy(subvalues, 0, concatenated, i, subvalues.length);
        i += subvalues.length;
      } else if (value != null && !value.isEmpty()) {
        concatenated[i] = value;
        i++;
      }
    }
    return new ConcatenatedBytes(concatenated, totalSize);
  }

  static Bytes create(List<Bytes> values) {
    if (values.isEmpty()) {
      return EMPTY;
    }
    if (values.size() == 1) {
      return values.getFirst();
    }

    int count = 0;
    int totalSize = 0;

    for (Bytes value : values) {
      int size = value.size();
      try {
        totalSize = Math.addExact(totalSize, size);
      } catch (ArithmeticException e) {
        throw new IllegalArgumentException(
            "Combined length of values is too long (> Integer.MAX_VALUE)");
      }
      if (value instanceof ConcatenatedBytes) {
        count += ((ConcatenatedBytes) value).values.length;
      } else if (size != 0) {
        count += 1;
      }
    }

    if (count == 0) {
      return EMPTY;
    }
    if (count == values.size()) {
      return new ConcatenatedBytes(values.toArray(new Bytes[0]), totalSize);
    }

    Bytes[] concatenated = new Bytes[count];
    int i = 0;
    for (Bytes value : values) {
      if (value instanceof ConcatenatedBytes) {
        Bytes[] subvalues = ((ConcatenatedBytes) value).values;
        System.arraycopy(subvalues, 0, concatenated, i, subvalues.length);
        i += subvalues.length;
      } else if (!value.isEmpty()) {
        concatenated[i++] = value;
      }
    }
    return new ConcatenatedBytes(concatenated, totalSize);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public byte get(int i) {
    checkElementIndex(i, size);
    for (Bytes value : values) {
      int vSize = value.size();
      if (i < vSize) {
        return value.get(i);
      }
      i -= vSize;
    }
    throw new IllegalStateException("element sizes do not match total size");
  }

  @Override
  public Bytes slice(int offset, int length) {
    if (offset == 0 && length == size) {
      return this;
    }
    if (length == 0) {
      return EMPTY;
    }

    checkElementIndex(offset, size);
    checkArgument(
        (offset + length) <= size,
        "Provided length %s is too large: the value has size %s and has only %s bytes from %s",
        length,
        size,
        size - offset,
        offset);

    int startIndex = 0;
    for (int i = 0; i < values.length; i++) {
      int currentValueSize = values[i].size();
      if (offset < currentValueSize) {
        startIndex = i;
        break;
      }
      offset -= currentValueSize;
    }

    int endIndex = values.length - 1;
    int lastElementLength = offset + length;
    for (int i = startIndex; i < values.length; i++) {
      int currentValueSize = values[i].size();
      if (lastElementLength <= currentValueSize) {
        endIndex = i;
        break;
      }
      lastElementLength -= currentValueSize;
    }

    if (startIndex == endIndex) {
      return values[startIndex].slice(offset, lastElementLength - offset);
    }

    Bytes[] combined = new Bytes[endIndex - startIndex + 1];
    combined[0] = values[startIndex].slice(offset);
    combined[combined.length - 1] = values[endIndex].slice(0, lastElementLength);

    if (endIndex >= startIndex + 2) {
      System.arraycopy(values, startIndex + 1, combined, 1, endIndex - startIndex - 1);
    }

    return new ConcatenatedBytes(combined, length);
  }

  @Override
  public MutableBytes mutableCopy() {
    return MutableBytes.fromArray(toArrayUnsafe());
  }

  @Override
  public void update(MessageDigest digest) {
    for (Bytes value : values) {
      value.update(digest);
    }
  }

  @Override
  public byte[] toArrayUnsafe() {
    byte[] bytesArray = new byte[size];
    int offset = 0;
    for (Bytes value : values) {
      System.arraycopy(value.toArrayUnsafe(), 0, bytesArray, offset, value.size());
      offset += value.size();
    }
    return bytesArray;
  }

  @Override
  protected void and(byte[] bytesArray, int offset, int length) {
    int resultOffset = offset;
    for (Bytes value : values) {
      value.and(bytesArray, resultOffset, value.size());
      resultOffset += value.size();
      if (resultOffset >= length) {
        return;
      }
    }
  }

  @Override
  protected void or(byte[] bytesArray, int offset, int length) {
    int resultOffset = offset;
    for (Bytes value : values) {
      value.or(bytesArray, resultOffset, value.size());
      resultOffset += value.size();
      if (resultOffset >= length) {
        return;
      }
    }
  }

  @Override
  protected void xor(byte[] bytesArray, int offset, int length) {
    int resultOffset = offset;
    for (Bytes value : values) {
      value.xor(bytesArray, resultOffset, value.size());
      resultOffset += value.size();
      if (resultOffset >= length) {
        return;
      }
    }
  }
}

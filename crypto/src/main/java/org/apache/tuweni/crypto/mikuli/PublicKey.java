// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.crypto.mikuli;

import org.apache.tuweni.v2.bytes.Bytes;

import java.util.List;
import java.util.Objects;

/** This class represents a BLS12-381 public key. */
public final class PublicKey {

  /**
   * Aggregates list of PublicKey pairs
   *
   * @param keys The list of public keys to aggregate, not null
   * @return PublicKey The public key, not null
   * @throws IllegalArgumentException if parameter list is empty
   */
  public static PublicKey aggregate(List<PublicKey> keys) {
    if (keys.isEmpty()) {
      throw new IllegalArgumentException("Parameter list is empty");
    }
    return keys.stream().reduce((a, b) -> a.combine(b)).get();
  }

  /**
   * Create a PublicKey from byte array
   *
   * @param bytes the bytes to read the public key from
   * @return a valid public key
   */
  public static PublicKey fromBytes(byte[] bytes) {
    return fromBytes(Bytes.wrap(bytes));
  }

  /**
   * Create a PublicKey from bytes
   *
   * @param bytes the bytes to read the public key from
   * @return a valid public key
   */
  public static PublicKey fromBytes(Bytes bytes) {
    G1Point point = G1Point.fromBytes(bytes);
    return new PublicKey(point);
  }

  private final G1Point point;

  PublicKey(G1Point point) {
    this.point = point;
  }

  PublicKey combine(PublicKey pk) {
    return new PublicKey(point.add(pk.point));
  }

  /**
   * Public key serialization
   *
   * @return byte array representation of the public key
   */
  public byte[] toByteArray() {
    return point.toBytes().toArrayUnsafe();
  }

  /**
   * Public key serialization
   *
   * @return byte array representation of the public key
   */
  public Bytes toBytes() {
    return point.toBytes();
  }

  G1Point g1Point() {
    return point;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Objects.hashCode(point);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (Objects.isNull(obj)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PublicKey other)) {
      return false;
    }
    return point.equals(other.point);
  }
}

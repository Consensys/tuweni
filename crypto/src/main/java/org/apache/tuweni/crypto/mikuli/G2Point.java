// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.crypto.mikuli;

import org.apache.milagro.amcl.BLS381.BIG;
import org.apache.milagro.amcl.BLS381.ECP2;
import org.apache.tuweni.v2.bytes.Bytes;

import java.util.Objects;

/**
 * G2 is the subgroup of elliptic curve similar to G1 and the points are identical except for where
 * they are elements of the extension field Fq12.
 */
final class G2Point implements Group<G2Point> {
  private final ECP2 point;
  private static final int fpPointSize = BIG.MODBYTES;

  G2Point(ECP2 point) {
    this.point = point;
  }

  @Override
  public G2Point add(G2Point other) {
    ECP2 sum = new ECP2();
    sum.add(point);
    sum.add(other.point);
    sum.affine();
    return new G2Point(sum);
  }

  @Override
  public G2Point mul(Scalar scalar) {
    ECP2 newPoint = point.mul(scalar.value());
    return new G2Point(newPoint);
  }

  Bytes toBytes() {
    byte[] bytes = new byte[4 * fpPointSize];
    point.toBytes(bytes);
    return Bytes.wrap(bytes);
  }

  static G2Point fromBytes(Bytes bytes) {
    return new G2Point(ECP2.fromBytes(bytes.toArrayUnsafe()));
  }

  ECP2 ecp2Point() {
    return point;
  }

  @Override
  public String toString() {
    return point.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (Objects.isNull(obj)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof G2Point other)) {
      return false;
    }
    return point.equals(other.point);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long xa = point.getX().getA().norm();
    long ya = point.getY().getA().norm();
    long xb = point.getX().getB().norm();
    long yb = point.getY().getB().norm();
    result = prime * result + (int) (xa ^ (xa >>> 32));
    result = prime * result + (int) (ya ^ (ya >>> 32));
    result = prime * result + (int) (xb ^ (xb >>> 32));
    result = prime * result + (int) (yb ^ (yb >>> 32));
    return result;
  }
}

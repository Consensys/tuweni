// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.ssz;

/** An object that can be written to a {@link SSZWriter}. */
public interface SSZWritable extends SSZType {
  void writeTo(SSZWriter writer);
}

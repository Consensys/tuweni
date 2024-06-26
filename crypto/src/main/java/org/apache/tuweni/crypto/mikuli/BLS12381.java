// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.crypto.mikuli;

import org.apache.milagro.amcl.BLS381.ECP;
import org.apache.milagro.amcl.BLS381.ECP2;
import org.apache.milagro.amcl.BLS381.MPIN;
import org.apache.tuweni.bytes.Bytes;

/*
 * Adapted from the ConsenSys/mikuli (Apache 2 License) implementation:
 * https://github.com/ConsenSys/mikuli/blob/master/src/main/java/net/consensys/mikuli/crypto/*.java
 */

/**
 * This Boneh-Lynn-Shacham (BLS) signature implementation is constructed from a pairing friendly
 * elliptic curve, the BLS12-381 curve. It uses parameters as defined in
 * https://z.cash/blog/new-snark-curve and the points in groups G1 and G2 are defined
 * https://github.com/zkcrypto/pairing/blob/master/src/bls12_381/README.md
 *
 * <p>This class depends upon the Apache Milagro library being available. See
 * https://milagro.apache.org.
 *
 * <p>Apache Milagro can be included using the gradle dependency
 * 'org.miracl.milagro.amcl:milagro-crypto-java'.
 */
public final class BLS12381 {

  private BLS12381() {}

  /**
   * Generates a SignatureAndPublicKey.
   *
   * @param keyPair The public and private key pair, not null
   * @param message The message to sign, not null
   * @param domain The domain value added to the message
   * @return The SignatureAndPublicKey, not null
   */
  public static SignatureAndPublicKey sign(KeyPair keyPair, byte[] message, int domain) {
    G2Point hashInGroup2 = hashFunction(message, domain);
    /*
     * The signature is hash point in G2 multiplied by the private key.
     */
    G2Point sig = keyPair.secretKey().sign(hashInGroup2);
    return new SignatureAndPublicKey(new Signature(sig), keyPair.publicKey());
  }

  /**
   * Generates a SignatureAndPublicKey.
   *
   * @param keyPair The public and private key pair, not null
   * @param message The message to sign, not null
   * @param domain The domain value added to the message
   * @return The SignatureAndPublicKey, not null
   */
  public static SignatureAndPublicKey sign(KeyPair keyPair, Bytes message, int domain) {
    return sign(keyPair, message.toArray(), domain);
  }

  /**
   * Verifies the given BLS signature against the message bytes using the public key.
   *
   * @param publicKey The public key, not null
   * @param signature The signature, not null
   * @param message The message data to verify, not null
   * @param domain The domain value added to the message
   * @return True if the verification is successful.
   */
  public static boolean verify(
      PublicKey publicKey, Signature signature, byte[] message, int domain) {
    G1Point g1Generator = KeyPair.g1Generator;

    G2Point hashInGroup2 = hashFunction(message, domain);
    GTPoint e1 = AtePairing.pair(publicKey.g1Point(), hashInGroup2);
    GTPoint e2 = AtePairing.pair(g1Generator, signature.g2Point());

    return e1.equals(e2);
  }

  /**
   * Verifies the given BLS signature against the message bytes using the public key.
   *
   * @param publicKey The public key, not null
   * @param signature The signature, not null
   * @param message The message data to verify, not null
   * @param domain The domain value added to the message
   * @return True if the verification is successful.
   */
  public static boolean verify(
      PublicKey publicKey, Signature signature, Bytes message, int domain) {
    return verify(publicKey, signature, message.toArrayUnsafe(), domain);
  }

  /**
   * Verifies the given BLS signature against the message bytes using the public key.
   *
   * @param sigAndPubKey The signature and public key, not null
   * @param message The message data to verify, not null
   * @param domain The domain value added to the message
   * @return True if the verification is successful, not null
   */
  public static boolean verify(SignatureAndPublicKey sigAndPubKey, byte[] message, int domain) {
    return verify(sigAndPubKey.publicKey(), sigAndPubKey.signature(), message, domain);
  }

  /**
   * Verifies the given BLS signature against the message bytes using the public key.
   *
   * @param sigAndPubKey The public key, not null
   * @param message The message data to verify, not null
   * @param domain The domain value added to the message
   * @return True if the verification is successful.
   */
  public static boolean verify(SignatureAndPublicKey sigAndPubKey, Bytes message, int domain) {
    return verify(sigAndPubKey.publicKey(), sigAndPubKey.signature(), message, domain);
  }

  private static G2Point hashFunction(byte[] message, int domain) {
    byte[] hashByte = MPIN.HASH_ID(ECP.SHA256, message, domain);
    return new G2Point(ECP2.mapit(hashByte));
  }
}

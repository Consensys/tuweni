// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.crypto.sodium;

import org.apache.tuweni.bytes.v2.Bytes;

import javax.security.auth.Destroyable;

import jnr.ffi.Pointer;

/** Message authentication code support for HMAC-SHA-512. */
public final class HMACSHA512 {

  private HMACSHA512() {}

  /** A HMACSHA512 secret key. */
  public static final class Key implements Destroyable {
    final Allocated value;

    Key(Pointer ptr, int length) {
      this.value = new Allocated(ptr, length);
    }

    @Override
    public void destroy() {
      value.destroy();
    }

    @Override
    public boolean isDestroyed() {
      return value.isDestroyed();
    }

    /**
     * Create a {@link Key} from an array of bytes.
     *
     * <p>The byte array must be of length {@link #length()}.
     *
     * @param bytes The bytes for the secret key.
     * @return A secret key.
     */
    public static Key fromBytes(Bytes bytes) {
      return fromBytes(bytes.toArrayUnsafe());
    }

    /**
     * Create a {@link Key} from an array of bytes.
     *
     * <p>The byte array must be of length {@link #length()}.
     *
     * @param bytes The bytes for the secret key.
     * @return A secret key.
     */
    public static Key fromBytes(byte[] bytes) {
      if (bytes.length != Sodium.crypto_auth_hmacsha512_keybytes()) {
        throw new IllegalArgumentException(
            "key must be "
                + Sodium.crypto_auth_hmacsha512_keybytes()
                + " bytes, got "
                + bytes.length);
      }
      return Sodium.dup(bytes, Key::new);
    }

    /**
     * Generate a random {@link Key}.
     *
     * @return A randomly generated secret key.
     */
    public static Key random() {
      return Sodium.randomBytes(length(), Key::new);
    }

    /**
     * Obtain the length of the key in bytes (32).
     *
     * @return The length of the key in bytes (32).
     */
    public static int length() {
      long keybytes = Sodium.crypto_auth_hmacsha512_keybytes();
      if (keybytes > Integer.MAX_VALUE) {
        throw new SodiumException("crypto_auth_hmacsha512_keybytes: " + keybytes + " is too large");
      }
      return (int) keybytes;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof Key)) {
        return false;
      }
      Key other = (Key) obj;
      return other.value.equals(value);
    }

    @Override
    public int hashCode() {
      return value.hashCode();
    }

    /**
     * Provides the bytes of this key.
     *
     * @return The bytes of this key.
     */
    public Bytes bytes() {
      return value.bytes();
    }

    /**
     * Provides the bytes of this key.
     *
     * @return The bytes of this key.
     */
    public byte[] bytesArray() {
      return value.bytesArray();
    }
  }

  /**
   * Authenticates a message using a secret into a HMAC-SHA-512 authenticator.
   *
   * @param message the message to authenticate
   * @param key the secret key to use for authentication
   * @return the authenticator of the message
   */
  public static Bytes authenticate(Bytes message, Key key) {
    return Bytes.wrap(authenticate(message.toArrayUnsafe(), key));
  }

  /**
   * Authenticates a message using a secret into a HMAC-SHA-512 authenticator.
   *
   * @param message the message to authenticate
   * @param key the secret key to use for authentication
   * @return the authenticator of the message
   */
  public static byte[] authenticate(byte[] message, Key key) {
    if (key.isDestroyed()) {
      throw new IllegalStateException("SecretKey has been destroyed");
    }
    long authBytes = Sodium.crypto_auth_hmacsha512_bytes();
    if (authBytes > Integer.MAX_VALUE) {
      throw new SodiumException("crypto_auth_hmacsha512_bytes: " + authBytes + " is too large");
    }
    byte[] out = new byte[(int) authBytes];
    int rc = Sodium.crypto_auth_hmacsha512(out, message, message.length, key.value.pointer());
    if (rc != 0) {
      throw new SodiumException("crypto_auth_hmacsha512: failed with result " + rc);
    }
    return out;
  }

  /**
   * Verifies the authenticator of a message matches according to a secret.
   *
   * @param authenticator the authenticator to verify
   * @param in the message to match against the authenticator
   * @param key the secret key to use for verification
   * @return true if the authenticator verifies the message according to the secret, false otherwise
   */
  public static boolean verify(Bytes authenticator, Bytes in, Key key) {
    return verify(authenticator.toArrayUnsafe(), in.toArrayUnsafe(), key);
  }

  /**
   * Verifies the authenticator of a message matches according to a secret.
   *
   * @param authenticator the authenticator to verify
   * @param in the message to match against the authenticator
   * @param key the secret key to use for verification
   * @return true if the authenticator verifies the message according to the secret, false otherwise
   */
  public static boolean verify(byte[] authenticator, byte[] in, Key key) {
    if (key.isDestroyed()) {
      throw new IllegalStateException("key has been destroyed");
    }
    if (authenticator.length != Sodium.crypto_auth_hmacsha512_bytes()) {
      throw new IllegalArgumentException(
          "Expected authenticator of "
              + Sodium.crypto_auth_hmacsha512_bytes()
              + " bytes, got "
              + authenticator.length
              + " instead");
    }
    int rc =
        Sodium.crypto_auth_hmacsha512_verify(authenticator, in, in.length, key.value.pointer());
    return rc == 0;
  }
}

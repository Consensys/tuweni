// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.net.tls;

import static org.apache.tuweni.net.tls.TLS.readPemFile;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.tuweni.junit.BouncyCastleExtension;
import org.apache.tuweni.junit.TempDirectory;
import org.apache.tuweni.junit.TempDirectoryExtension;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TempDirectoryExtension.class)
@ExtendWith(BouncyCastleExtension.class)
class TLSTest {

  @Test
  void createCertificateIfFilesAreNotThere(@TempDirectory Path tempDir) throws Exception {
    Path certificate = tempDir.resolve("foo").resolve("server.crt");
    Path key = tempDir.resolve("foo").resolve("server.key");

    boolean wasCreated = TLS.createSelfSignedCertificateIfMissing(key, certificate);
    assertTrue(wasCreated);

    assertTrue(Files.exists(certificate));
    assertTrue(Files.exists(key));
  }

  @Test
  void doesNotGenerateSelfSignedCertificateIfCertFileExists(@TempDirectory Path tempDir)
      throws Exception {
    Path certificate = tempDir.resolve("server1.crt");
    Path key = tempDir.resolve("server1.key");

    Files.createFile(certificate);
    boolean wasCreated = TLS.createSelfSignedCertificateIfMissing(key, certificate);
    assertFalse(wasCreated);

    assertTrue(Files.exists(certificate));
    assertFalse(Files.exists(key));
  }

  @Test
  void doesNotGenerateSelfSignedCertificateIfKeyFileExists(@TempDirectory Path tempDir)
      throws Exception {
    Path certificate = tempDir.resolve("server2.crt");
    Path key = tempDir.resolve("server2.key");

    Files.createFile(key);
    boolean wasCreated = TLS.createSelfSignedCertificateIfMissing(key, certificate);
    assertFalse(wasCreated);

    assertFalse(Files.exists(certificate));
    assertTrue(Files.exists(key));
  }

  @Test
  void autoGeneratedCertsAreValid(@TempDirectory Path tempDir) throws Exception {
    Path certificate = tempDir.resolve("server3.crt");
    Path key = tempDir.resolve("server3.key");

    boolean wasCreated = TLS.createSelfSignedCertificateIfMissing(key, certificate);
    assertTrue(wasCreated);

    checkKeyPair(key, certificate);
  }

  private void checkKeyPair(Path key, Path cert) throws Exception {
    PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(readPemFile(key));
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    Certificate certificate =
        cf.generateCertificate(new ByteArrayInputStream(Files.readAllBytes(cert)));
    KeyFactory kf = KeyFactory.getInstance("RSA");
    KeyPair keyPair = new KeyPair(certificate.getPublicKey(), kf.generatePrivate(pkcs8KeySpec));

    byte[] challenge = new byte[10000];
    ThreadLocalRandom.current().nextBytes(challenge);

    // sign using the private key
    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initSign(keyPair.getPrivate());
    sig.update(challenge);
    byte[] signature = sig.sign();

    // verify signature using the public key
    sig.initVerify(keyPair.getPublic());
    sig.update(challenge);

    assertTrue(sig.verify(signature));
  }
}

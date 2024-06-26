// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.net.tls;

import static org.apache.tuweni.net.tls.SecurityTestUtils.DUMMY_FINGERPRINT;
import static org.apache.tuweni.net.tls.TLS.certificateHexFingerprint;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.tuweni.junit.TempDirectory;
import org.apache.tuweni.junit.TempDirectoryExtension;
import org.apache.tuweni.junit.VertxExtension;
import org.apache.tuweni.junit.VertxInstance;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import io.vertx.core.Vertx;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.SelfSignedCertificate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TempDirectoryExtension.class)
@ExtendWith(VertxExtension.class)
class ServerCaOrTofaTest {

  private static HttpClient caClient;
  private static String fooFingerprint;
  private static HttpClient fooClient;
  private static HttpClient foobarClient;

  private Path knownClientsFile;
  private HttpServer httpServer;

  @BeforeAll
  static void setupClients(@TempDirectory Path tempDir, @VertxInstance Vertx vertx)
      throws Exception {
    SelfSignedCertificate caClientCert = SelfSignedCertificate.create();
    SecurityTestUtils.configureJDKTrustStore(tempDir, caClientCert);
    caClient =
        vertx.createHttpClient(
            new HttpClientOptions()
                .setTrustOptions(InsecureTrustOptions.INSTANCE)
                .setSsl(true)
                .setKeyCertOptions(caClientCert.keyCertOptions()));

    SelfSignedCertificate fooCert = SelfSignedCertificate.create("foo.com");
    fooFingerprint = certificateHexFingerprint(Paths.get(fooCert.keyCertOptions().getCertPath()));
    HttpClientOptions fooClientOptions = new HttpClientOptions();
    fooClientOptions
        .setSsl(true)
        .setKeyCertOptions(fooCert.keyCertOptions())
        .setTrustOptions(InsecureTrustOptions.INSTANCE)
        .setConnectTimeout(1500)
        .setReuseAddress(true)
        .setReusePort(true);
    fooClient = vertx.createHttpClient(fooClientOptions);

    SelfSignedCertificate foobarCert = SelfSignedCertificate.create("foobar.com");
    HttpClientOptions foobarClientOptions = new HttpClientOptions();
    foobarClientOptions
        .setSsl(true)
        .setKeyCertOptions(foobarCert.keyCertOptions())
        .setTrustOptions(InsecureTrustOptions.INSTANCE)
        .setConnectTimeout(1500)
        .setReuseAddress(true)
        .setReusePort(true);
    foobarClient = vertx.createHttpClient(foobarClientOptions);
  }

  @BeforeEach
  void startServer(@TempDirectory Path tempDir, @VertxInstance Vertx vertx) throws Exception {
    knownClientsFile = tempDir.resolve("known-clients.txt");
    Files.write(knownClientsFile, Arrays.asList("#First line", "foobar.com " + DUMMY_FINGERPRINT));

    SelfSignedCertificate serverCert = SelfSignedCertificate.create();
    HttpServerOptions options = new HttpServerOptions();
    options
        .setSsl(true)
        .setClientAuth(ClientAuth.REQUIRED)
        .setPemKeyCertOptions(serverCert.keyCertOptions())
        .setTrustOptions(VertxTrustOptions.trustClientOnFirstAccess(knownClientsFile))
        .setIdleTimeout(1500)
        .setReuseAddress(true)
        .setReusePort(true);
    httpServer = vertx.createHttpServer(options);
    SecurityTestUtils.configureAndStartTestServer(httpServer);
  }

  @AfterEach
  void stopServer() {
    httpServer.close();
  }

  @AfterAll
  static void cleanupClients() {
    caClient.close();
    fooClient.close();
    foobarClient.close();
  }

  @Test
  void shouldValidateUsingCertificate() throws Exception {
    CompletableFuture<HttpClientResponse> respFuture = new CompletableFuture<>();
    caClient
        .request(HttpMethod.GET, httpServer.actualPort(), "localhost", "/upcheck")
        .onSuccess((req) -> req.send().onSuccess(respFuture::complete));
    HttpClientResponse resp = respFuture.join();
    assertEquals(200, resp.statusCode());

    List<String> knownClients = Files.readAllLines(knownClientsFile);
    assertEquals(2, knownClients.size(), "CA verified host should not have been recorded");
    assertEquals("#First line", knownClients.get(0));
    assertEquals("foobar.com " + DUMMY_FINGERPRINT, knownClients.get(1));
  }

  @Test
  void shouldValidateOnFirstUse() throws Exception {
    CompletableFuture<HttpClientResponse> respFuture = new CompletableFuture<>();
    fooClient
        .request(HttpMethod.GET, httpServer.actualPort(), "localhost", "/upcheck")
        .onSuccess((req) -> req.send().onSuccess(respFuture::complete));
    HttpClientResponse resp = respFuture.join();
    assertEquals(200, resp.statusCode());

    List<String> knownClients = Files.readAllLines(knownClientsFile);
    assertEquals(3, knownClients.size());
    assertEquals("#First line", knownClients.get(0));
    assertEquals("foobar.com " + DUMMY_FINGERPRINT, knownClients.get(1));
    assertEquals("foo.com " + fooFingerprint, knownClients.get(2));
  }

  @Test
  void shouldRejectDifferentCertificate() {
    CompletableFuture<HttpClientResponse> respFuture = new CompletableFuture<>();
    foobarClient
        .request(HttpMethod.GET, httpServer.actualPort(), "localhost", "/upcheck")
        .onFailure(respFuture::completeExceptionally)
        .onSuccess(
            (req) ->
                req.send()
                    .onFailure(respFuture::completeExceptionally)
                    .onSuccess(respFuture::complete));
    Throwable e = assertThrows(CompletionException.class, respFuture::join);
    e = e.getCause().getCause();
    assertTrue(e.getMessage().contains("certificate_unknown"));
  }
}

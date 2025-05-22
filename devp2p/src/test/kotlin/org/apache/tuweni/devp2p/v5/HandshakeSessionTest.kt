// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.devp2p.v5

import io.vertx.core.net.SocketAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.apache.tuweni.bytes.Bytes
import org.apache.tuweni.bytes.Bytes32
import org.apache.tuweni.concurrent.coroutines.await
import org.apache.tuweni.crypto.SECP256K1
import org.apache.tuweni.crypto.SECP256K1.PublicKey
import org.apache.tuweni.crypto.SECP256K1.SecretKey
import org.apache.tuweni.devp2p.EthereumNodeRecord
import org.apache.tuweni.devp2p.v5.encrypt.AES128GCM
import org.apache.tuweni.devp2p.v5.encrypt.SessionKeyGenerator
import org.apache.tuweni.junit.BouncyCastleExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.net.InetAddress

@ExtendWith(BouncyCastleExtension::class)
class HandshakeSessionTest {

  @Test
  fun testConnectTwoClients() =
    runBlocking {
      val secretKey = SecretKey.fromBytes(Bytes32.fromHexString("0x01"))
      val publicKey = PublicKey.fromSecretKey(secretKey)
      val keyPair = SECP256K1.KeyPair.create(secretKey, publicKey)
      val peerSecretKey = SecretKey.fromBytes(Bytes32.fromHexString("0x02"))
      val peerPublicKey = PublicKey.fromSecretKey(peerSecretKey)
      val peerKeyPair = SECP256K1.KeyPair.create(peerSecretKey, peerPublicKey)
      val address = SocketAddress.inetSocketAddress(1234, "localhost")
      val peerAddress = SocketAddress.inetSocketAddress(1235, "localhost")
      val enr = EthereumNodeRecord.create(keyPair, ip = InetAddress.getLoopbackAddress(), udp = 1234)
      val peerEnr = EthereumNodeRecord.create(peerKeyPair, ip = InetAddress.getLoopbackAddress(), udp = 1235)
      var peerSession: HandshakeSession? = null

      val session =
        HandshakeSession(
          keyPair,
          peerAddress,
          peerKeyPair.publicKey(),
          { _, message -> runBlocking { peerSession!!.processMessage(message) } },
          { enr },
          Dispatchers.Default,
        )
      peerSession =
        HandshakeSession(
          peerKeyPair,
          address,
          keyPair.publicKey(),
          { _, message -> runBlocking { session.processMessage(message) } },
          { peerEnr },
          Dispatchers.Default,
        )

      val key = session.connect().await()
      val peerKey = peerSession.awaitConnection().await()
      assertEquals(key, peerKey)
    }

  @Test
  fun testInitiatorAndRecipientKey() {
    val keyPair = SECP256K1.KeyPair.random()
    val peerKeyPair = SECP256K1.KeyPair.random()
    val ephemeralKeyPair = SECP256K1.KeyPair.random()
    val enr = EthereumNodeRecord.create(keyPair, ip = InetAddress.getLoopbackAddress(), udp = 1234)
    val peerEnr = EthereumNodeRecord.create(peerKeyPair, ip = InetAddress.getLoopbackAddress(), udp = 1235)
    val secret = SECP256K1.deriveECDHKeyAgreement(ephemeralKeyPair.secretKey().bytes(), keyPair.publicKey().bytes())
    val nonce = Bytes.random(12)
    val session = SessionKeyGenerator.generate(enr.nodeId(), peerEnr.nodeId(), secret, nonce)
    val peerSession = SessionKeyGenerator.generate(enr.nodeId(), peerEnr.nodeId(), secret, nonce)
    val authTag = Message.authTag()
    val token = Message.authTag()
    val encryptedMessage = AES128GCM.encrypt(
      session.initiatorKey,
      authTag,
      Bytes.wrap("hello world".toByteArray()),
      token,
    )
    val decryptedMessage = AES128GCM.decrypt(peerSession.initiatorKey, authTag, encryptedMessage, token)
    assertEquals(Bytes.wrap("hello world".toByteArray()), decryptedMessage)
  }
}

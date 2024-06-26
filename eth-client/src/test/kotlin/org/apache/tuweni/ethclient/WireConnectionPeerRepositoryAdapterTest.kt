// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.ethclient

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.apache.tuweni.crypto.SECP256K1
import org.apache.tuweni.junit.BouncyCastleExtension
import org.apache.tuweni.rlpx.wire.WireConnection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions

@ExtendWith(BouncyCastleExtension::class)
class WireConnectionPeerRepositoryAdapterTest {

  @Test
  fun testCloseNoop() {
    val repository = mock(EthereumPeerRepository::class.java)
    val connRepo = WireConnectionPeerRepositoryAdapter(repository)
    connRepo.close()
    verifyNoInteractions(repository)
  }

  @Test
  fun addPeer() {
    val repository = MemoryEthereumPeerRepository()
    val connRepo = WireConnectionPeerRepositoryAdapter(repository)
    val pubKey = SECP256K1.KeyPair.random().publicKey()
    val conn = mock<WireConnection> {
      on { peerHost() } doReturn("example.com")
      on { peerPort() } doReturn(1234)
      on { peerPublicKey() } doReturn(pubKey)
      on { uri() }.doReturn("foo")
    }

    connRepo.add(conn)
    assertEquals(1, repository.identities.size)
    val identity = repository.identities.first()
    assertEquals("example.com", identity.networkInterface())
    assertEquals(1234, identity.port())

    assertEquals(1, repository.connections.size)
    assertEquals(1, repository.peerMap.size)
  }

  @Test
  fun addAndGet() {
    val repository = MemoryEthereumPeerRepository()
    val connRepo = WireConnectionPeerRepositoryAdapter(repository)
    val pubKey = SECP256K1.KeyPair.random().publicKey()
    val conn = mock<WireConnection> {
      on { peerHost() } doReturn("example.com")
      on { peerPort() } doReturn(1234)
      on { peerPublicKey() } doReturn(pubKey)
      on { uri() }.doReturn("foo")
    }
    val id = connRepo.add(conn)
    assertEquals(conn, connRepo[id])
  }
}

// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.devp2p.v5

import io.vertx.core.Vertx
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.tuweni.concurrent.coroutines.await
import org.apache.tuweni.crypto.Hash
import org.apache.tuweni.crypto.SECP256K1
import org.apache.tuweni.devp2p.EthereumNodeRecord
import org.apache.tuweni.junit.BouncyCastleExtension
import org.apache.tuweni.junit.VertxExtension
import org.apache.tuweni.junit.VertxInstance
import org.apache.tuweni.v2.bytes.Bytes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.ConcurrentHashMap

internal class SimpleTestENRStorage : ENRStorage {

  val storage: MutableMap<Bytes, EthereumNodeRecord> = ConcurrentHashMap()

  override fun find(nodeId: Bytes): EthereumNodeRecord? = storage[nodeId]

  override fun put(nodeId: Bytes, enr: EthereumNodeRecord) {
    storage[nodeId] = enr
  }
}

@ExtendWith(BouncyCastleExtension::class, VertxExtension::class)
class ConnectTwoServersTest {

  @Test
  fun testConnectTwoServers(@VertxInstance vertx: Vertx) = runBlocking {
    val storage = SimpleTestENRStorage()
    val service = DiscoveryService.open(
      vertx,
      SECP256K1.KeyPair.random(),
      localPort = 40000,
      bootstrapENRList = emptyList(),
      enrStorage = storage,
    )
    service.start().await()

    val otherStorage = SimpleTestENRStorage()
    val otherService = DiscoveryService.open(
      vertx,
      SECP256K1.KeyPair.random(),
      localPort = 40001,
      bootstrapENRList = emptyList(),
      enrStorage = otherStorage,
    )
    otherService.start().await()
    otherService.addPeer(service.enr()).await()
    delay(500)
    assertEquals(1, storage.storage.size)
    assertEquals(1, otherStorage.storage.size)
    assertNotNull(otherStorage.find(Hash.sha2_256(service.enr().toRLP())))
    assertNotNull(storage.find(Hash.sha2_256(otherService.enr().toRLP())))

    service.terminate()
    otherService.terminate()
  }
}

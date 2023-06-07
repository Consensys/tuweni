// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.wallet

import org.apache.tuweni.bytes.Bytes
import org.apache.tuweni.crypto.sodium.Sodium
import org.apache.tuweni.junit.BouncyCastleExtension
import org.apache.tuweni.junit.TempDirectory
import org.apache.tuweni.junit.TempDirectoryExtension
import org.apache.tuweni.units.bigints.UInt256
import org.apache.tuweni.units.ethereum.Gas
import org.apache.tuweni.units.ethereum.Wei
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Path
import java.nio.file.Paths

@ExtendWith(TempDirectoryExtension::class, BouncyCastleExtension::class)
class WalletTest {

  companion object {
    @JvmStatic
    @BeforeAll
    fun setup() {
      Assumptions.assumeTrue(Sodium.isAvailable(), "Sodium native library is not available")
    }
  }

  @Test
  fun testCreate(@TempDirectory tempDir: Path) {
    val wallet = Wallet.create(tempDir.resolve(Paths.get("subfolder", "mywallet")), "password")
    val tx = wallet.sign(
      UInt256.valueOf(0),
      Wei.valueOf(3),
      Gas.valueOf(22),
      null,
      Wei.valueOf(2),
      Bytes.EMPTY,
      1,
    )
    assertTrue(wallet.verify(tx))
  }

  @Test
  fun testCreateAndOpen(@TempDirectory tempDir: Path) {
    val wallet = Wallet.create(tempDir.resolve(Paths.get("subfolder", "mywallet")), "password")
    val tx = wallet.sign(
      UInt256.valueOf(0),
      Wei.valueOf(3),
      Gas.valueOf(22),
      null,
      Wei.valueOf(2),
      Bytes.EMPTY,
      1,
    )
    val wallet2 = Wallet.open(tempDir.resolve(Paths.get("subfolder", "mywallet")), "password")
    assertTrue(wallet2.verify(tx))
  }
}

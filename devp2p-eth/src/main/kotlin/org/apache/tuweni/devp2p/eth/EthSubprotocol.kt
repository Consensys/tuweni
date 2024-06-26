// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.devp2p.eth

import kotlinx.coroutines.Dispatchers
import org.apache.tuweni.eth.repository.BlockchainRepository
import org.apache.tuweni.eth.repository.TransactionPool
import org.apache.tuweni.rlpx.RLPxService
import org.apache.tuweni.rlpx.WireConnectionRepository
import org.apache.tuweni.rlpx.wire.SubProtocol
import org.apache.tuweni.rlpx.wire.SubProtocolClient
import org.apache.tuweni.rlpx.wire.SubProtocolHandler
import org.apache.tuweni.rlpx.wire.SubProtocolIdentifier
import org.apache.tuweni.rlpx.wire.WireConnection
import kotlin.coroutines.CoroutineContext

class EthSubprotocol(
  private val coroutineContext: CoroutineContext = Dispatchers.Default,
  private val blockchainInfo: BlockchainInformation,
  private val repository: BlockchainRepository,
  private val pendingTransactionsPool: TransactionPool,
  private val selectionStrategy:
  (WireConnectionRepository) -> ConnectionSelectionStrategy = ::RandomConnectionSelectionStrategy,
  private val listener: (WireConnection, Status) -> Unit = { _, _ -> },
) : SubProtocol {

  companion object {
    val ETH62 = SubProtocolIdentifier.of("eth", 62, 8)
    val ETH63 = SubProtocolIdentifier.of("eth", 63, 17)
    val ETH64 = SubProtocolIdentifier.of("eth", 64, 17)
    val ETH65 = SubProtocolIdentifier.of("eth", 65, 17)
    val ETH66 = SubProtocolIdentifier.of("eth", 66, 17)
  }

  override fun id(): SubProtocolIdentifier = ETH66

  override fun supports(subProtocolIdentifier: SubProtocolIdentifier): Boolean {
    return "eth".equals(subProtocolIdentifier.name()) && (
      subProtocolIdentifier.version() == ETH62.version() ||
        subProtocolIdentifier.version() == ETH63.version() || subProtocolIdentifier.version() == ETH64.version() ||
        subProtocolIdentifier.version() == ETH65.version() || subProtocolIdentifier.version() == ETH66.version()
      )
  }

  override fun createHandler(service: RLPxService, client: SubProtocolClient): SubProtocolHandler {
    val controller = EthController(repository, pendingTransactionsPool, client as EthRequestsManager, listener)
    if (client is EthClient66) {
      return EthHandler66(coroutineContext, blockchainInfo, service, controller)
    } else {
      return EthHandler(coroutineContext, blockchainInfo, service, controller)
    }
  }

  override fun getCapabilities() = mutableListOf(ETH62, ETH63, ETH64, ETH65, ETH66)

  override fun createClient(service: RLPxService, identifier: SubProtocolIdentifier): SubProtocolClient {
    if (identifier == ETH66) {
      return EthClient66(
        service,
        pendingTransactionsPool,
        selectionStrategy(service.repository()),
      )
    } else {
      return EthClient(service, pendingTransactionsPool, selectionStrategy(service.repository()))
    }
  }
}

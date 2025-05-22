// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.devp2p

import org.apache.tuweni.crypto.Hash
import org.apache.tuweni.crypto.SECP256K1
import org.apache.tuweni.rlp.RLP
import org.apache.tuweni.rlp.RLPReader
import org.apache.tuweni.rlp.RLPWriter
import org.apache.tuweni.units.bigints.UInt256
import org.apache.tuweni.v2.bytes.Bytes
import org.apache.tuweni.v2.bytes.MutableBytes
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import java.net.InetAddress
import java.time.Instant

/**
 * Ethereum Node Record (ENR) as described in [EIP-778](https://eips.ethereum.org/EIPS/eip-778).
 *
 * @param signature the record signature
 * @param seq the sequence of the record, its revision number
 * @param data the arbitrary data of the record
 * @param listData the arbitrary data of the record as list
 */
class EthereumNodeRecord(
  val signature: Bytes,
  val seq: Long,
  val data: Map<String, Bytes>,
  val listData: Map<String, List<Bytes>> = emptyMap(),
  val rlp: Bytes,
) {

  companion object {

    /**
     * Derives the public key of an ethereum node record into a unique 32 bytes hash.
     * @param publicKey the public key to hash
     * @return the hash of the public key
     * @throws IllegalArgumentException if the public key is not valid.
     */
    fun nodeId(publicKey: SECP256K1.PublicKey): Bytes {
      val pt = publicKey.asEcPoint()
      val xPart = UInt256.valueOf(pt.xCoord.toBigInteger())
      val yPart = UInt256.valueOf(pt.yCoord.toBigInteger())
      return Hash.keccak256(Bytes.wrap(xPart, yPart))
    }

    /**
     * Creates an ENR from its serialized form as a RLP list
     * @param rlp the serialized form of the ENR
     * @return the ENR
     * @throws IllegalArgumentException if the rlp bytes length is longer than 300 bytes
     */
    @JvmStatic
    fun fromRLP(rlp: Bytes): EthereumNodeRecord {
      if (rlp.size() > 300) {
        throw IllegalArgumentException("Record too long")
      }
      return RLP.decodeList(rlp) { fromRLP(it, rlp) }
    }

    /**
     * Creates an ENR from its serialized form as a RLP list
     * @param reader the RLP reader
     * @return the ENR
     * @throws IllegalArgumentException if the rlp bytes length is longer than 300 bytes
     */
    @JvmStatic
    fun fromRLP(reader: RLPReader): EthereumNodeRecord {
      val tempRecord = fromRLP(reader, Bytes.EMPTY)
      val encoded = RLP.encodeList {
        it.writeValue(tempRecord.signature)
        encode(data = tempRecord.data, seq = tempRecord.seq, writer = it)
      }

      return fromRLP(encoded)
    }

    /**
     * Creates an ENR from its serialized form as a RLP list
     * @param reader the RLP reader
     * @return the ENR
     * @throws IllegalArgumentException if the rlp bytes length is longer than 300 bytes
     */
    @JvmStatic
    fun fromRLP(reader: RLPReader, rlp: Bytes): EthereumNodeRecord {
      val sig = reader.readValue()

      val seq = reader.readLong()

      val data = mutableMapOf<String, Bytes>()
      val listData = mutableMapOf<String, List<Bytes>>()
      while (!reader.isComplete) {
        val key = reader.readString()
        if (reader.nextIsList()) {
          listData[key] = reader.readListContents { listreader ->
            if (listreader.nextIsList()) {
              // TODO complex structures not supported
              listreader.skipNext()
              null
            } else {
              listreader.readValue()
            }
          }.filterNotNull()
        } else {
          val value = reader.readValue()
          data[key] = value
        }
      }

      return EthereumNodeRecord(sig, seq, data, listData, rlp)
    }

    fun encode(
      signatureKeyPair: SECP256K1.KeyPair? = null,
      seq: Long = Instant.now().toEpochMilli(),
      ip: InetAddress? = null,
      tcp: Int? = null,
      udp: Int? = null,
      data: Map<String, Bytes>? = null,
      listData: Map<String, List<Bytes>>? = null,
      writer: RLPWriter,
    ) {
      writer.writeLong(seq)
      val mutableData = data?.toMutableMap() ?: mutableMapOf()
      mutableData["id"] = Bytes.wrap("v4".toByteArray())
      signatureKeyPair?.let {
        mutableData["secp256k1"] = Bytes.wrap(it.publicKey().asEcPoint().getEncoded(true))
      }
      ip?.let {
        mutableData["ip"] = Bytes.wrap(it.address)
      }
      tcp?.let {
        mutableData["tcp"] = Bytes.ofUnsignedShort(it)
      }
      udp?.let {
        mutableData["udp"] = Bytes.ofUnsignedShort(it)
      }
      val keys = mutableListOf<String>()
      keys.addAll(mutableData.keys)
      listData?.let { keys.addAll(it.keys) }
      keys.sorted().forEach { key ->
        mutableData[key]?.let { value ->
          writer.writeString(key)
          writer.writeValue(value)
        }
        listData?.get(key)?.let { value ->
          writer.writeString(key)
          writer.writeList(value) { writer, v -> writer.writeValue(v) }
        }
      }
    }

    /**
     * Creates the serialized form of a ENR
     * @param signatureKeyPair the key pair to use to sign the ENR
     * @param seq the sequence number for the ENR. It should be higher than the previous time the ENR was generated. It defaults to the current time since epoch in milliseconds.
     * @param data the key pairs to encode in the ENR
     * @param listData the key pairs of list values to encode in the ENR
     * @param ip the IP address of the host
     * @param tcp an optional parameter to a TCP port used for the wire protocol
     * @param udp an optional parameter to a UDP port used for discovery
     * @return the ENR
     */
    @JvmOverloads
    @JvmStatic
    fun create(
      signatureKeyPair: SECP256K1.KeyPair,
      seq: Long = Instant.now().toEpochMilli(),
      data: Map<String, Bytes>? = null,
      listData: Map<String, List<Bytes>>? = null,
      ip: InetAddress,
      tcp: Int? = null,
      udp: Int? = null,
    ): EthereumNodeRecord {
      return fromRLP(toRLP(signatureKeyPair, seq, data, listData, ip, tcp, udp))
    }

    /**
     * Creates the serialized form of a ENR
     * @param signatureKeyPair the key pair to use to sign the ENR
     * @param seq the sequence number for the ENR. It should be higher than the previous time the ENR was generated. It defaults to the current time since epoch in milliseconds.
     * @param data the key pairs to encode in the ENR
     * @param listData the key pairs of list values to encode in the ENR
     * @param ip the IP address of the host
     * @param tcp an optional parameter to a TCP port used for the wire protocol
     * @param udp an optional parameter to a UDP port used for discovery
     * @return the serialized form of the ENR as a RLP-encoded list
     */
    @JvmOverloads
    @JvmStatic
    fun toRLP(
      signatureKeyPair: SECP256K1.KeyPair,
      seq: Long = Instant.now().toEpochMilli(),
      data: Map<String, Bytes>? = null,
      listData: Map<String, List<Bytes>>? = null,
      ip: InetAddress,
      tcp: Int? = null,
      udp: Int? = null,
    ): Bytes {
      val encoded = RLP.encodeList { writer ->
        encode(signatureKeyPair, seq, ip, tcp, udp, data, listData, writer)
      }
      val signature = SECP256K1.sign(encoded, signatureKeyPair)
      val sigBytes = MutableBytes.create(64)
      sigBytes.set(0, UInt256.valueOf(signature.r()))
      sigBytes.set(32, UInt256.valueOf(signature.s()))

      val completeEncoding = RLP.encodeList { writer ->
        writer.writeValue(sigBytes)
        encode(signatureKeyPair, seq, ip, tcp, udp, data, listData, writer)
      }
      return completeEncoding
    }
  }

  /**
   * Validates an ENR to check that it conforms to a valid ENR scheme.
   *
   * Only the v4 scheme is supported at this time.
   */
  fun validate() {
    if (Bytes.wrap("v4".toByteArray()) != data["id"]) {
      throw InvalidNodeRecordException("id attribute is not set to v4")
    }

    val encoded = RLP.encodeList {
      encode(data = data, seq = seq, writer = it)
    }

    val sig = SECP256K1.Signature.create(
      1,
      signature.slice(0, 32).toUnsignedBigInteger(),
      signature.slice(32).toUnsignedBigInteger(),
    )

    val pubKey = publicKey()
    val recovered = SECP256K1.PublicKey.recoverFromSignature(encoded, sig)

    if (pubKey != recovered) {
      val sig0 = SECP256K1.Signature.create(
        0,
        signature.slice(0, 32).toUnsignedBigInteger(),
        signature.slice(32).toUnsignedBigInteger(),
      )
      val recovered0 = SECP256K1.PublicKey.recoverFromSignature(encoded, sig0)
      if (pubKey != recovered0) {
        throw InvalidNodeRecordException("Public key does not match signature")
      }
    }
  }

  /**
   * The ENR public key entry
   * @return the ENR public key
   */
  fun publicKey(): SECP256K1.PublicKey {
    return SECP256K1.PublicKey.fromBytes(publicKeyBytes())
  }

  /**
   * The ENR public key entry bytes
   * @return the ENR public key bytes
   */
  private fun publicKeyBytes(): Bytes {
    val keyBytes = data["secp256k1"] ?: throw InvalidNodeRecordException("Missing secp256k1 entry")
    val ecPoint = SECP256K1.Parameters.CURVE.curve.decodePoint(keyBytes.toArrayUnsafe())
    return Bytes.wrap(ecPoint.getEncoded(false)).slice(1)
  }

  /**
   * Derives the public key of an ethereum node record into a unique 32 bytes hash.
   * @return the hash of the public key
   */
  fun nodeId() = nodeId(publicKey())

  /**
   * The ip associated with the ENR
   * @return The IP adress of the ENR
   */
  fun ip(): InetAddress {
    return data["ip"]?.let { InetAddress.getByAddress(it.toArrayUnsafe()) } ?: InetAddress.getLoopbackAddress()
  }

  /**
   * The TCP port of the ENR
   * @return the TCP port associated with this ENR
   */
  fun tcp(): Int? {
    return data["tcp"]?.toInt()
  }

  /**
   * The UDP port of the ENR
   * @return the UDP port associated with this ENR
   */
  fun udp(): Int? {
    return data["udp"]?.toInt() ?: tcp()
  }

  fun seq(): Long {
    return seq
  }

  /**
   * @return the ENR as a URI
   */
  override fun toString(): String {
    return "enr:${ip()}:${tcp()}?udp=${udp()}"
  }

  fun toRLP(): Bytes = rlp

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as EthereumNodeRecord

    return rlp == other.rlp
  }

  override fun hashCode(): Int {
    return rlp.hashCode()
  }
}

internal class InvalidNodeRecordException(message: String?) : RuntimeException(message)

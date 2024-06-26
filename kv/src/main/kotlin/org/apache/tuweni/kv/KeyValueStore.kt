// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.kv

import kotlinx.coroutines.CoroutineScope
import org.apache.tuweni.concurrent.AsyncCompletion
import org.apache.tuweni.concurrent.AsyncResult
import org.apache.tuweni.concurrent.coroutines.asyncCompletion
import org.apache.tuweni.concurrent.coroutines.asyncResult
import java.io.Closeable

/**
 * A key-value store.
 */
interface KeyValueStore<K, V> : Closeable, CoroutineScope {

  /**
   * Returns true if the store contains the key.
   *
   * @param key The key for the content.
   * @return true if an entry with the key exists in the store.
   */
  suspend fun containsKey(key: K): Boolean

  /**
   * Returns true if the store contains the key.
   *
   * @param key The key for the content.
   * @return An [AsyncResult] that will complete with a boolean result.
   */
  fun containsKeyAsync(key: K): AsyncResult<Boolean> = asyncResult { containsKey(key) }

  /**
   * Retrieves data from the store.
   *
   * @param key The key for the content.
   * @return The stored data, or null if no data was stored under the specified key.
   */
  suspend fun get(key: K): V?

  /**
   * Retrieves data from the store.
   *
   * @param key The key for the content.
   * @return An [AsyncResult] that will complete with the stored content,
   *         or an empty optional if no content was available.
   */
  fun getAsync(key: K): AsyncResult<V?> = asyncResult { get(key) }

  /**
   * Puts data into the store.
   *
   * @param key The key to associate with the data, for use when retrieving.
   * @param value The data to store.
   */
  suspend fun put(key: K, value: V)

  /**
   * Removes data from the store.
   *
   * @param key The key to associate with the data, for use when retrieving.
   */
  suspend fun remove(key: K)

  /**
   * Puts data into the store.
   *
   * Note: if the storage implementation already contains content for the given key, it does not need to replace the
   * existing content.
   *
   * @param key The key to associate with the data, for use when retrieving.
   * @param value The data to store.
   * @return An [AsyncCompletion] that will complete when the content is stored.
   */
  fun putAsync(key: K, value: V): AsyncCompletion = asyncCompletion { put(key, value) }

  /**
   * Provides an iterator over the keys of the store.
   *
   * @return An [Iterable] allowing to iterate over the set of keys.
   */
  suspend fun keys(): Iterable<K>

  /**
   * Provides an iterator over the keys of the store.
   *
   * @return An [Iterable] allowing to iterate over the set of keys.
   */
  fun keysAsync(): AsyncResult<Iterable<K>> = asyncResult { keys() }

  /**
   * Clears the contents of the store.
   */
  suspend fun clear()

  /**
   * Clears the contents of the store.
   * @return An [AsyncCompletion] that will complete when the content is cleared.
   */
  fun clearAsync() = asyncCompletion { clear() }
}

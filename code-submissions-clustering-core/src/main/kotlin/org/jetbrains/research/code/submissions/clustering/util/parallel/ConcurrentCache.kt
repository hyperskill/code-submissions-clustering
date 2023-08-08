package org.jetbrains.research.code.submissions.clustering.util.parallel

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

class ConcurrentCache<K, V> {
    private val cache = ConcurrentHashMap<K, Deferred<V>>()

    suspend fun get(key: K): V? = cache[key]?.await()

    operator fun set(key: K, value: V) {
        cache[key] = CompletableDeferred(value)
    }

    suspend fun computeOrUpdate(key: K, computeNewOrUpdate: suspend (V?) -> V): V = coroutineScope {
        cache[key]?.let {
            return@coroutineScope computeNewOrUpdate(it.await())
        }

        val deferred = async(start = CoroutineStart.LAZY) {
            computeNewOrUpdate(null)
        }

        cache.putIfAbsent(key, deferred)?.let {
            return@coroutineScope computeNewOrUpdate(it.await())
        }
        deferred.await()
    }

    fun clear() = cache.clear()
}

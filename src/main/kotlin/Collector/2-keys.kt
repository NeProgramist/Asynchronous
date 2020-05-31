package Collector

import kotlinx.coroutines.*
import java.lang.Error

class KeyCollector<T, K>(
    private val keys: List<T>,
    timeout: Long = 0L,
    cb: (error: Throwable?, data: Map<T, K>?) -> Unit
) {
    private val expected = keys.size
    private val collected = mutableMapOf<T, K>()
    private var finished = false

    private var timer: Job? = if (timeout > 0L) GlobalScope.launch {
        delay(timeout)
        if (finished) return@launch
        finished = true
        cb(Error("Collector timed out"), null)
    } else null

    private val onDone = { error: Throwable?, data: Map<T, K>? ->
        timer?.cancel()
        finished = true
        cb(error, data)
    }

    fun collect(key: T, data: K? = null, error: Throwable? = null) = when {
        finished -> false
        key !in keys -> false
        key in collected.keys -> false
        error != null -> {
            onDone(error, null)
            true
        }
        data == null -> false
        else -> {
            collected += key to data
            if (expected == collected.size) onDone(null, collected)
            true
        }
    }
    fun collect(pair: Pair<T, K>) = collect(pair.first, pair.second, null)
    fun collectError(pair: Pair<T, Throwable>) = collect(pair.first, null, pair.second)
}

// Usage
private fun main() = runBlocking {
    val keys = listOf("key1", "key2", "key3")
    val kc1 = KeyCollector<String, Int>(keys, 1000L) { error, data ->
        println("kc1")
        println("Error: $error, Data: $data")
    }

    kc1.collect("key1" to 1)
    kc1.collect("key2" to 2)
    kc1.collect("key3" to 3)

    val kc2 = KeyCollector<String, Int>(keys, 1000L) { error, data ->
        println("kc2")
        println("Error: $error, Data: $data")
    }

    kc2.collect("key1" to 1)
    kc2.collect("key2" to 2)
    kc2.collect("key4" to 4)

    val kc3 = KeyCollector<String, Int>(keys, 1000L) { error, data ->
        println("kc3")
        println("Error: $error, Data: $data")
    }

    kc3.collectError("key1" to Error("Collect an error"))
    kc3.collect("key2" to 2)
    kc3.collect("key2" to 3)

    delay(1001L)
}

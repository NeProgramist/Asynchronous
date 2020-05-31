package Collector

import kotlinx.coroutines.*

class DataCollector<T, K>(
    private val expected: Int,
    timeout: Long = 0,
    cb: (error: Throwable?, data: Map<T, K>?) -> Unit
) {
    private var finished = false
    private var data = mutableMapOf<T, K>()
    private val timer = if (timeout > 0L) GlobalScope.launch {
        delay(timeout)

        if(finished) return@launch
        finished = true
        onDone(Error("Collector timed out"), null);
    } else null

    private val onDone: (error: Throwable?, data: Map<T, K>?) -> Unit = { error, data ->
        timer?.cancel()
        finished = true
        cb(error, data)
    }

    fun collect(key: T, error: Throwable?, value: K? = null) = when {
        finished -> Unit
        error != null -> onDone(error, null)
        value == null -> Unit
        else -> {
            data[key] = value
            if (data.size == expected) onDone(null, data)
            Unit
        }
    }

    fun collect(key: T, value: K) = collect(key, null, value)
    fun collect(pair: Pair<T, K>) = collect(pair.first, null, pair.second)
}

private fun main() = runBlocking {
    val dc1 = DataCollector<String, Int>(3, 1000) { error, data ->
        println("it's dc1")
        println("err: $error | data: $data")
    }

    dc1.collect("key1", 1)
    dc1.collect("key2", 2)
    dc1.collect("key3", 3)

    val dc2 = DataCollector<String, Int>(3, 1000) { error, data ->
        println("it's dc2")
        println("err: $error | data: $data")
    }

    dc2.collect("key1" to 1)
    dc2.collect("key2" to 2)

    val dc3 = DataCollector<String, Int>(3, 1000) { error, data ->
        println("it's dc3")
        println("err: $error | data: $data")
    }

    dc3.collect("key1", Error("Some unexpected error"))
    dc3.collect("key2", 2)
    dc3.collect("key3", 3)

    delay(1001)
}
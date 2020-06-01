package Collector

import kotlinx.coroutines.*

class Collector<T, K>() {
    constructor(limit: Int): this() {
        expectedLimit = limit
    }

    constructor(vararg keys: T): this(keys.toList())

    constructor(keys: List<T>): this() {
        expectedKeys = keys
    }

    constructor(lambda: Collector<T,K>.() -> Unit): this() {
        lambda()
    }

    private var expectedKeys = listOf<T>()
    private var expectedLimit: Int = 0
    private var keys = mutableSetOf<T>()
    private var finished = false
    private var onDoneListener: (error: Throwable?, data: Map<T, K>?) -> Unit = { _, _ ->
        timer?.cancel()
        timer = null
        finished = true
    }
    private var timer: Job? = null
    private val data= mutableMapOf<T, K>()

    fun onDone(cb: (error: Throwable?, data: Map<T, K>?) -> Unit): Collector<T, K> {
        onDoneListener = { error, data ->
            timer?.cancel()
            timer = null
            finished = true
            cb(error, data)
        }
        return this
    }

    fun collect(key: T, error: Throwable?, value: K?): Collector<T, K> = when {
        finished -> this
        value == null -> this
        error != null -> {
            onDoneListener(error, null)
            this
        }
        else -> {
            data[key] = value
            if (key in expectedKeys) keys.add(key)
            if (keys.size == expectedKeys.size && expectedLimit <= data.size) onDoneListener(null, data)
            this
        }
    }

    fun pick(key: T, value: K?) = collect(key, null, value)
    infix fun pick(pair: Pair<T, K>) = pick(pair.first, pair.second)

    fun fail(key: T, error: Throwable?) = collect(key, error, null)
    infix fun fail(pair: Pair<T, Throwable?>) = fail(pair.first, pair.second)

    infix fun failWith(error: Throwable?): Collector<T, K> {
        onDoneListener(error, null)
        return this
    }

    fun <V> take(key: T, arg: V? = null, func: (V?) -> K) = try {
        pick(key, func(arg))
    } catch(e: Throwable) {
        fail(key, e)
    }
    fun <V> take(key: T, pair: Pair<V, (V?) -> K>) = take(key, pair.first, pair.second)
    fun take(key: T, block: (Unit?) -> K) = take(key, Unit, block)

    infix fun timeout(timeout: Long): Collector<T, K> {
        timer?.cancel()
        timer = if (timeout > 0) GlobalScope.launch {
            delay(timeout)

            if (finished) return@launch
            finished = true
            onDoneListener(Error("Collector timed out"), null);
        } else null
        return this
    }

    infix fun limitToNum(n: Int): Collector<T, K> {
        expectedLimit = n
        return this
    }

    infix fun limitToKeys(keys: List<T>): Collector<T, K> {
        expectedKeys = keys
        return this
    }
    fun limitToKeys(vararg keys: T) = limitToKeys(keys.toList())
}

infix fun <A, B> Pair<A, B>.collectTo(collector: Collector<A, B>) = collector.pick(this)

private fun main() = runBlocking {
    val c1 = Collector<String, Int>(3)
    c1 timeout 1000L
    c1.onDone { error, data -> println("c1: Error = $error, data = $data") }

    "key1" to 1 collectTo c1
    "key2" to 2 collectTo c1
    c1.take("key3") { 1 + 2 }


    val c2 = Collector<String, Int>("key1", "key2", "key3")
    c2.onDone { error, data -> println("c2: Error = $error, data = $data") }
    c2.timeout(1000L)

    c2.pick("key1" to 1)
    c2.pick("key1" to 11)
    c2.pick("key2" to 2)
    c2.pick("key3" to 3)


    val c3 = Collector<String, Int> {
        limitToKeys("key1", "key2", "key3")
        limitToNum(5)
        timeout(1000L)
        onDone { error, data -> println("c3: Error = $error, data = $data") }
    }

    c3.pick("key1" to 1)
    c3.pick("key2" to 2)
    c3.pick("key3" to 3)
    c3.pick("key4" to 4)
    c3.pick("key5" to 5)

    return@runBlocking
}
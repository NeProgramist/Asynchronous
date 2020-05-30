package AsyncAdapter

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

typealias Suspback<T> = suspend (Throwable?, T?) -> Unit

// Deferred-returning function callback-last / error-first
fun <T, K> callbackify(fn: (T) -> Deferred<K>): suspend (T, Suspback<K>) -> Unit = { args, cb ->
    runCatching {
        cb(null, fn(args).await())
    }.onFailure {
        cb(it, null)
    }
}

// Usage
private fun twiceAsync(x: Int) = GlobalScope.async { x * 2 }
private val twiceCallback = callbackify(::twiceAsync)

private fun halfAsync(x: Int) = GlobalScope.async { x / 2 }
private val halfCallback = callbackify(::halfAsync)

private fun main() = runBlocking {
    val a = twiceAsync(100).await()
    val b = halfAsync(a).await()
    println("async: $b")

    twiceCallback(100) { _, value ->
        halfCallback(value!!) { _, result ->
            println("callback: $result")
        }
    }
}

package AsyncAdapter

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

typealias Suspback<T> = suspend (Throwable?, T?) -> Unit

// Deferred-returning function callback-last / error-first
fun <T, K> callbackify(def: Deferred<K>): suspend (Suspback<K>) -> Unit = { cb ->
    runCatching {
        cb(null, def.await())
    }.onFailure {
        cb(it, null)
    }
}

// Usage

private fun main() = runBlocking {
    val def1 = GlobalScope.async { "value" }

    val callback = callbackify<String, String>(def1)

    callback { error, value->
        println("value $value")
    }
}

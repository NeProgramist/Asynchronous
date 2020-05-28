package AsyncAdapter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

// Callback-last function to Promise-returning
fun <T, K>promisify(scope: CoroutineScope = GlobalScope, fn: (data: T, Errback<K>) -> Unit) = { arg: T ->
    var res: K? = null
    scope.async {
        fn(arg) { error, data ->
            if (error != null) throw error
            res = data
        }
        res
    }
}

// Usage
private fun twiceCallback(x: Int, callback: Errback<Int>) = callback(null, x * 2)
private val twicePromise = promisify(fn=::twiceCallback)
private fun halfCallback(x: Int, callback: Errback<Int>) = callback(null, x / 2)
private val halfPromise = promisify<Int, Int> { data, cb -> halfCallback(data, cb) }

private fun main() {
    twiceCallback(100) { _, value ->
        halfCallback(value!!) { _, result ->
            println("callbackLast: $result")
        }
    }

    runBlocking {
        val a = twicePromise(100).await()
        val b = halfPromise(a!!).await()
        println("promisified: $b")
    }
}

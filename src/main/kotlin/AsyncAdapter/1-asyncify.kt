package AsyncAdapter

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

typealias Errback<T> = (Error?, T?) -> Unit

fun <T, K> asyncify(fn: (T) -> K) = { arg:T, callback: Errback<K> ->
    GlobalScope.launch {
        val res = fn(arg)
        if (res is Error) callback(res, null)
        else callback(null, res)
    }
}

private fun main() = runBlocking{
    val twice = { x: Float -> x*2 }
    val twiceAsync = asyncify(twice)

    val half = { x: Float -> x/2 }
    val halfAsync = asyncify(half)

    val result = half(twice(100f))
    println("sync res: $result")

    twiceAsync(100f) { err1, value ->
        if (err1 != null) println("the error occurred(twice): $err1")
        halfAsync(value!!) { err2, result ->
            if (err2 != null) println("the error occurred(half): $err2")
            else println("async res: $result")
        }
    }.join()
}
package Future

import kotlin.math.pow

var i = 0

class DebugFuture<T>(
    private val executor: (resolve: (T) -> Unit, reject: (Error) -> Unit) -> Unit
) {
    private val id = i++

    init { println("new Future $id") }

    companion object {
        fun <T> of(value: T) = DebugFuture<T> { resolve, _ -> resolve(value) }
    }

    fun chain(fn: (T) -> DebugFuture<T>): DebugFuture<T> {
        println("chain $id")
        return DebugFuture<T> { resolve, reject -> fork(
            {
                println("resolve $id")
                fn(it).fork(resolve, reject)
            },
            { reject(it) }
        )}
    }

    fun map(fn: (T) -> T): DebugFuture<T> {
        println("map $id")
        return chain {
            println("map.chain $id")
            of(fn(it))
        }
    }

    fun fork(succeeded: (T) -> Unit, failed: (Error) -> Unit) {
        println("fork $id")
        executor(succeeded, failed)
    }
}

private fun main() {
    DebugFuture.of(5)
        .map { x -> x + 1 }
        .map { x -> x.toDouble().pow(3).toInt() }
        .map { x-> x * 2 }
        .fork (
            { x -> println("result: $x") },
            { err -> println("error: $err") }
    )
}

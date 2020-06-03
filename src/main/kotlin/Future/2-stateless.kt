package Future

import kotlin.math.pow

interface ChainableFuture<T>: Future<T> {
    fun chain(fn: (T) -> Future<T>): ChainableFuture<T>
}

private fun <T> future(executor: (resolve: (T) -> Unit) -> Unit): ChainableFuture<T> = object : ChainableFuture<T> {
    override fun chain(fn: (T) -> Future<T>) = future<T> { resolve ->
        fork { value -> fn(value).fork(resolve) }
    }

    override fun map(fn: (T) -> T) = chain { futureOf(fn(it)) }

    override fun fork(succedeed: (T) -> Unit): ChainableFuture<T> {
        executor(succedeed)
        return this
    }
}

private fun <T> futureOf(value: T) = future<T> { it(value) }

// Usage
private fun main() {
    val future1 = future<Int> { it(5) }
        .map {
            println("future1 started")
            it
        }
        .map { it + 1 }
        .map { it.toDouble().pow(3.0).toInt() }
        .fork { println("future1 result: $it") }

    println(future1)
}

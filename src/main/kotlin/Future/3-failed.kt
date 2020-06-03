package Future

import kotlin.math.pow

interface FailableFuture<T> {
    fun map(fn: (T) -> T): FailableFuture<T>
    fun fork(succeeded: (T) -> Unit, failed: (Throwable) -> Unit): FailableFuture<T>
    fun chain(fn: (T) -> FailableFuture<T>): FailableFuture<T>

}

private fun <T> future(
    executor: (resolve: (T) -> Unit, reject: (Throwable) -> Unit) -> Unit
): FailableFuture<T> = object: FailableFuture<T> {
    override fun chain(fn: (T) -> FailableFuture<T>) = future { resolve: (T) -> Unit, reject: (Throwable) -> Unit ->
        fork(
            { value -> fn(value).fork(resolve, reject) },
            { error -> reject(error) }
        )
    }

    override fun map(fn: (T) -> T) = chain { value -> futureOf(fn(value)) }

    override fun fork(succeeded: (T) -> Unit, failed: (Throwable) -> Unit): FailableFuture<T> {
        executor(succeeded, failed)
        return this
    }
}

private fun <T> futureOf(value: T) = future<T> { resolve, _ ->  resolve(value) }

private fun main() {
    future<Int> { _, reject -> reject(Error("rejected")) }
        .map { x ->
            println("future is started")
            x
        }
        .map { x -> x + 1 }
        .map { x -> x.toDouble().pow(3).toInt() }
        .fork (
            { x -> println("result: $x") },
            { err -> println("failed: $err") }
        )
}

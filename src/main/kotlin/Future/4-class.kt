package Future

import kotlin.math.pow

class FutureClass<T>(
    private val executor: (resolve: (T) -> Unit, reject: (Error) -> Unit) -> Unit
) {
    companion object {
        fun <T> of(value: T) = FutureClass<T> { resolve, _ -> resolve(value) }
    }

    fun chain(fn: (T) -> FutureClass<T>) = FutureClass<T> { resolve, reject -> fork(
        { fn(it).fork(resolve, reject) },
        { reject(it) }
    )}

    fun map(fn: (T) -> T) = chain { of(fn(it)) }

    fun fork(succeeded: (T) -> Unit, failed: (Error) -> Unit = {}) {
        executor(succeeded, failed)
    }
}

// Usage
private fun main() {
    FutureClass.of(6.0)
        .map {
            println("future1 started")
            it
        }
        .map { it + 1 }
        .map { it.pow(3) }
        .fork(
            { println("future result: $it") },
            { println("future failed: ${it.message}") }
        )
}

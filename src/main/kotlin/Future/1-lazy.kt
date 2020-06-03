package Future

import kotlin.math.pow

interface Future<T> {
    fun map(fn: (T) -> T): Future<T>
    fun fork(succeeded: (T) -> Unit): Future<T>
}

private fun <T> future(value: T): Future<T> {
    var mapper: ((T) -> T)? = null
    return object: Future<T> {
        override fun map(fn: (T) -> T): Future<T> {
            mapper = fn
            return future(this)
        }

        override fun fork(succeeded: (T) -> Unit): Future<T> {
            val finish = { result: T -> mapper?.let { succeeded(it(result)) } ?: succeeded(result) }
            finish(value)
            return this
        }
    }
}

private fun <T> future(value: Future<T>): Future<T> {
    var mapper: ((T) -> T)? = null
    return object: Future<T> {
        override fun map(fn: (T) -> T): Future<T> {
            mapper = fn
            return future(this)
        }

        override fun fork(succeeded: (T) -> Unit): Future<T> {
            val finish = { result: T -> mapper?.let { succeeded(it(result)) } ?: succeeded(result) }
            value.fork(finish)
            return this
        }
    }
}

private fun main() {
    val future1 = future(5)
        .map { x -> x + 1 }
        .map { x -> x.toDouble().pow(3).toInt() }
        .map { x ->
            println("result: $x")
            x
        }

    future1.fork {  }
}

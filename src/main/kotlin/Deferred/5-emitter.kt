package Deferred

import EventEmitter.EventEmitter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Error

sealed class DeferredResult<T> {
    data class Data<T>(val value: T): DeferredResult<T>()
    data class Error<T>(val error: Throwable): DeferredResult<T>()
}

class DeferredEE<T: Any>(
    onDone: ((T) -> Unit)? = null,
    onFail: ((Throwable) -> Unit)? = null
) : EventEmitter<DeferredResult<T>>(), Deferred<T> {
    override var status = Deferred.Status.DEFERRED_PENDING
    private lateinit var value: T
    private lateinit var error: Throwable

    init {
        if (onDone != null) on("done") {
            if (it is DeferredResult.Data<T>) onDone(it.value)
        }

        if (onFail != null) on("fail") {
            if (it is DeferredResult.Error) onFail(it.error)
        }
    }

    override fun done(callback: (T) -> Unit): DeferredEE<T> {
        on("done") {
            if (it is DeferredResult.Data<T>) callback(it.value)
        }

        if (isResolved) callback(value)
        return this
    }

    override fun fail(callback: (Throwable) -> Unit): DeferredEE<T> {
        on("fail") {
            if (it is DeferredResult.Error) callback(it.error)
        }
        if (isRejected) callback(error)
        return this
    }

    override fun resolve(data: T): DeferredEE<T> {
        value = data
        status = Deferred.Status.DEFERRED_RESOLVED
        emit("done", DeferredResult.Data(value))
        return this
    }

    override fun reject(reason: Throwable): DeferredEE<T> {
        error = reason
        status = Deferred.Status.DEFERRED_REJECTED
        emit("fail", DeferredResult.Error(error))
        return this
    }
}

// Usage

private fun main() = runBlocking {
    val persons = mapOf(
        10 to "Marcus Aurelius",
        11 to "Mao Zedong",
        12 to "Rene Descartes"
    )

    fun getPerson(id: Int): DeferredEE<String> {
        val result = DeferredEE<String>()
        GlobalScope.launch {
            delay(1000L)
            val name = persons[id]
            if (name != null) result.resolve(name)
            else result.reject(Error("Person is not found"))
        }
        return result
    }

    val d1 = getPerson(10)
        .done { println("Resolver d1: $it") }
        .fail { println("Resolver d1: ${it.message}") }
    println("d1 = $d1")

    val d2 = getPerson(20)
    d2.on("done") {
        if (it is DeferredResult.Data) println("Resolved d2: ${it.value}")
    }
    d2.on("fail") {
        if (it is DeferredResult.Error) println("Resolved d2: ${it.error.message}")
    }
    println("d2 = $d2")
    delay(2000L)
}

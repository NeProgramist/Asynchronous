package Deferred

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DeferredImpl<T: Any>(
    private var onDone: ((T) -> Unit)? = null,
    private var onFail: ((Throwable) -> Unit)? = null
): Deferred<T> {
    private lateinit var value: T
    private lateinit var error: Throwable
    override var status = Deferred.Status.DEFERRED_PENDING

    override fun done(callback: (T) -> Unit): Deferred<T> {
        onDone = callback
        if (isResolved) callback(value)
        return this
    }

    override fun fail(callback: (Throwable) -> Unit): Deferred<T> {
        onFail = callback
        if (isRejected) callback(error)
        return this
    }

    override fun resolve(data: T): Deferred<T> {
        value = data
        status = Deferred.Status.DEFERRED_RESOLVED
        onDone?.invoke(value)
        return this
    }

    override fun reject(err: Throwable): Deferred<T> {
        error = err
        status = Deferred.Status.DEFERRED_REJECTED
        onFail?.invoke(error)
        return this
    }
}


private fun main() = runBlocking {
    val persons = mapOf(
        10 to "Marcus Aurelius",
        11 to "Mao Zedong",
        12 to "Rene Descartes"
    ).withDefault { "" }

    fun getPerson(id: Int): Deferred<String> {
        val result = DeferredImpl<String>()
        GlobalScope.launch {
            delay(1000)
            if (persons.getValue(id) != "") result.resolve(persons.getValue(id))
            else result.reject(Error("Can't find that file"))
        }
        return result
    }

    val d1 = getPerson(10)
        .done { value -> println("value d1: $value") }
        .fail { error -> print("error occurred d1: $error")}
    println(d1.toString())


    val d2 = getPerson(20)
        .done { value -> println("value d2: $value") }
        .fail { error -> print("error occurred d2: $error")}
    println(d2.toString())

    delay(2000)
}
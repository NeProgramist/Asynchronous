package Deferred

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

enum class DeferredState(val state: Int) {
    DEFERRED_PENDING(0),
    DEFERRED_RESOLVED(1),
    DEFERRED_REJECTED(2)
}

interface Deferred<T> {
    var status: DeferredState
    var onDone: ((T) -> Unit)?
    var onFail: ((T) -> Unit)?

    val isPending
        get() = status == DeferredState.DEFERRED_PENDING
    val isResolved
        get() = status == DeferredState.DEFERRED_RESOLVED
    val isRejected
        get() = status == DeferredState.DEFERRED_REJECTED

    fun done(callback: (T) -> Unit): Deferred<T>
    fun fail(callback: (T) -> Unit): Deferred<T>
    fun resolve(data: T): Deferred<T>
    fun reject(data: T): Deferred<T>
}

private fun <T: Any> deferred() = object: Deferred<T> {
    private lateinit var value: T
    override var status = DeferredState.DEFERRED_PENDING
    override var onDone: ((T) -> Unit)? = null
    override var onFail: ((T) -> Unit)? = null

    override fun done(callback: (T) -> Unit): Deferred<T> {
        onDone = callback
        if (isResolved) callback(value)
        return this
    }

    override fun fail(callback: (T) -> Unit): Deferred<T> {
        onFail = callback
        if (isRejected) callback(value)
        return this
    }

    override fun resolve(data: T): Deferred<T> {
        value = data
        status = DeferredState.DEFERRED_RESOLVED
        onDone?.invoke(value)
        return this
    }

    override fun reject(data: T): Deferred<T> {
        value = data
        status = DeferredState.DEFERRED_REJECTED
        onFail?.invoke(value)
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
        val result = deferred<String>()
        GlobalScope.launch {
            delay(1000)
            if (persons.getValue(id) != "") result.resolve(persons.getValue(id))
            else result.reject("Can't find that person")
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

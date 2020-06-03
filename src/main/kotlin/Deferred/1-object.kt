package Deferred

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

interface PrimaryDeferred<T> {
    fun done(callback: (T) -> Unit): PrimaryDeferred<T>
    fun resolve(data: T): PrimaryDeferred<T>
}

private fun <T: Any> asyncResult() = object:  PrimaryDeferred<T> {
    private lateinit var value: T
    private var onDone: ((T) -> Unit)? = null

    override fun done(callback: (T) -> Unit): PrimaryDeferred<T> {
        onDone = callback
        return this
    }

    override fun resolve(data: T): PrimaryDeferred<T> {
        value = data
        onDone?.invoke(value)
        return this
    }
}

private fun main() = runBlocking {
    val persons = mapOf(
        10 to "Marcus Aurelius",
        11 to "Mao Zedong",
        12 to "Rene Descartes"
    ).withDefault { "" }

    fun getPerson(id: Int): PrimaryDeferred<String> {
        val result = asyncResult<String>()
        GlobalScope.launch {
            delay(1000)
            result.resolve(persons.getValue(id))
        }
        return result
    }

    val d1 = getPerson(10)
    d1.done { value ->
        println("value d1: $value")
    }

    val d2 = getPerson(11)
    GlobalScope.launch {
        delay(1500)
        d2.done { value ->
            println("value d2: $value")
        }
    }

    delay(2000)
}

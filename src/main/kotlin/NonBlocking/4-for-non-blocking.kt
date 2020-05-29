package NonBlocking

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.system.measureTimeMillis

private fun main() = runBlocking {
    val numbers = flow { for (i in 0..999) emit(i) }
    var k = 0

    val interval = launch(newSingleThreadContext("Interval")) {
        repeat(Int.MAX_VALUE) {
            delay(10)
            println("next ${k++} on ${Thread.currentThread().name}")
        }
    }

    val time = measureTimeMillis {
        numbers.collect { println("$it on ${Thread.currentThread().name}") }
    }
    interval.cancel()

    println("Time(ms): $time")
    println("k = $k")
}

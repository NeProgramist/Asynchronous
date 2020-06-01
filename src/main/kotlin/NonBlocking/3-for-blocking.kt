package NonBlocking

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

private fun main() = runBlocking {
    val numbers = List(1_000_000) {it}
    var k = 0

    val interval = launch {
        repeat(Int.MAX_VALUE) {
            delay(10)
            println("next ${k++} on ${Thread.currentThread().name}")
        }
    }

    val time = measureTimeMillis {
        numbers.forEach { println("$it on ${Thread.currentThread().name}") }
    }
    interval.cancel()

    println("Time(ms): $time")
    println("k = $k")
}

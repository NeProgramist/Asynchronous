package NonBlocking

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

private fun main() = runBlocking {
    val numbers = List(1000) { it } // вводіть 1_000_000 для значущої різниці
    var k = 0

    val interval = launch(newSingleThreadContext("Interval")) {
        repeat(Int.MAX_VALUE) {
            delay(10)
            println("next ${k++}")
        }
    }

    val time = measureTimeMillis {
        numbers.map {
            GlobalScope.launch { println(it) }
        }.joinAll()
        interval.cancel()
    }

    println("Time(ms): $time")
    println("k = $k")
}

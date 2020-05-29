package NonBlocking

import kotlinx.coroutines.delay
import kotlin.system.measureTimeMillis


val numbers = List(1000) {it}

suspend fun main() {
    val time = measureTimeMillis {
        numbers.forEach { println(it) }
        delay(1)
        println("i'm here")
    }

    println("Blocking $time")
}

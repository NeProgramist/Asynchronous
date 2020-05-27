package Timers

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

suspend fun main() {
    println("start ${Date()}")
    val job = sleep(3000)
    job.invokeOnCompletion {
        println("end ${Date()}")
    }

    job.join()
}

private fun sleep(msec: Long) = GlobalScope.launch {
    delay(msec)
}
package timers

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

    delay(10000)
}

private fun sleep(msec: Long) = GlobalScope.launch {
    delay(msec)
}
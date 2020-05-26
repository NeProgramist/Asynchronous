package timers

import kotlinx.coroutines.*
import java.util.*

private fun sleep(msec: Long) = GlobalScope.async { delay(msec) }

suspend fun main() {
    println("Start sleep: ${Date()}")
    println("   Sleep about 3 sec")
    sleep(3000).await()
    println("After sleep: ${Date()}")
}

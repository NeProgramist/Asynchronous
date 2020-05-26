package timers

import java.time.LocalDateTime
import java.util.*

private fun sleep(msec: Int) {
    val end = Date().time + msec
    while (Date().time < end) {}
}

fun main() {
    println("start ${Date()}")
    sleep(3000)
    println("end ${Date()}")
}


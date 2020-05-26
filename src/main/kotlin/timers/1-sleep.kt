package timers

import java.util.*

fun main() {
    println("start")
    sleep(3000)
    println("end")
}

fun sleep(msec: Int) {
    val end = Date().time + msec
    while (Date().time < end) {}
}
package timers

import kotlinx.coroutines.*

private fun setTimeout(msec: Long, cb: () -> Unit) = GlobalScope.launch {
    delay(msec)
    cb()
}

private fun setInterval(msec: Long, cb: () -> Unit): Job = GlobalScope.launch {
    async {
        delay(msec)
        cb()
    }.await()
    setInterval(msec, cb)
}

suspend fun main() {
    setTimeout(0) { println("callback #1 setTimeout 0") }
    setTimeout(0) { println("callback #2 setTimeout 0") }
    setTimeout(1) { println("callback #3 setTimeout 1") }


    println("callback #5 in main")
    println("callback #6 in main")

    setInterval(0) { println("callback #7 setInterval 0") }.cancel()
    setInterval(0) { println("callback #8 setInterval 0") }.cancel()

    delay(500)
    println("Coroutines done")
}
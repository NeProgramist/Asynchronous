package Timers

import kotlinx.coroutines.*

private fun setTimeout(msec: Long, cb: () -> Unit) = GlobalScope.launch {
    delay(msec)
    cb()
}

private tailrec suspend fun setInterval(msec: Long, cb: suspend () -> Unit) {
    delay(msec)
    cb()
    return setInterval(msec, cb)
}

fun main() = runBlocking {
    setTimeout(0) { println("callback #1 setTimeout 0") }
    setTimeout(0) { println("callback #2 setTimeout 0") }
    setTimeout(1) { println("callback #3 setTimeout 1") }


    println("callback #5 in main")
    println("callback #6 in main")

    lateinit var job1: Job
    job1 = launch {
        setInterval(1) { // 0 causes StackOverflow as nothing really gets delayed
            println("callback #7 setInterval 1")
            job1.cancel()
        }
    }

    lateinit var job2: Job
    job2 = launch {
        setInterval(1) {
            println("callback #8 setInterval 1")
            job2.cancel()
        }
    }

    println("Main done")
}

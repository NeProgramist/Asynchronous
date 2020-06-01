package Timers

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Timer(private val interval: Long) {
    private var enabled = false
    private val listeners = mutableListOf<() -> Unit>()
    private lateinit var job: Job

    fun on(name:String, func: () -> Unit) {
        if (name == "timer") listeners.add(func)
    }

    fun start() {
        if (!enabled) {
            enabled = true
            job = GlobalScope.launch {
                delay(interval)
                for (func in listeners) func()
            }
        }
        println("timer start")
    }

    fun stop() {
        if (enabled) {
            job.cancel()
            enabled = false
            println("timer stop")
        }
    }
}

suspend fun main() {
    val timer = Timer(3000)

    timer.on("timer") { println("i'm here") }
    timer.on("timer") { println("i'm here x2") }
    timer.on("not a timer") { println("i'm not here") }

    timer.start()
    delay(2000)
    timer.stop()
    timer.start()
    delay(10000)
}

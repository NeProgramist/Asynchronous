package RevealingConstructor

import kotlinx.coroutines.*
import kotlinx.coroutines.time.withTimeout

private class Timer(
    private val interval: Long,
    private val listener: () -> Unit
) {
    private var enabled = false
    private var job: Job? = null

    fun start() {
        if (enabled) return
        enabled = true
        job = GlobalScope.launch {
            delay(interval)
            listener()
        }
    }

    fun stop() {
        if (enabled) {
            job?.cancel()
            enabled = false
        }
    }
}

// Usage
private fun main() = runBlocking {
    val timer = Timer(1000L) { println("Timer event") }
    timer.start()
    withTimeout(500L) { timer.stop() }
    delay(1001L)
}

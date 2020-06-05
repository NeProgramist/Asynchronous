package ConcurrentQueue

import kotlinx.coroutines.*
import java.lang.Error

open class SuspendQueue<T: TimedTask>(
    concurrency: Int,
    scope: CoroutineScope = GlobalScope
): TimeoutQueue<T>(concurrency, scope) {
    var paused = false
    protected val hasChannel: Boolean
        get() = count < this.concurrency

    override fun add(task: T) {
        if (!paused) super.add(task)
        else {
            task.startTime = now
            task.waitTimeout = waitTimeout
            waiting.add(task)
        }
    }

    override fun next(task: T) {
        count++
        var finished = false
        var timer: Job? = null

        fun end(error: Error? = null, res: T) {
            if (finished) return
            finished = true
            timer?.cancel()
            count--
            finish(error, res)
            if (!paused and waiting.isNotEmpty()) takeNext()
        }

        if (processTimeout != Long.MAX_VALUE) {
            timer = scope.launch {
                delay(processTimeout)
                end(Error("Process timed out."), task)
            }
        }

        scope.launch {
            onProcess?.invoke(task, ::end)
        }
    }

    override fun takeNext() {
        val task = shiftTask()

        if (task isExpiredBy now) {
            finish(Error("Waiting timed out"), task)
            if (!paused and waiting.isNotEmpty()) takeNext()
            return
        }

        if (hasChannel) next(task)
    }

    fun pause(): SuspendQueue<T> {
        paused = true
        return this
    }

    fun resume(): SuspendQueue<T> {
        if (waiting.isNotEmpty()) {
            val channels = concurrency - count
            for (i in 0 until channels) takeNext()
        }

        paused = false
        return this
    }
}

// Usage

private fun main() = runBlocking {
    suspend fun job(task: TimedTask, next: Errback<TimedTask>) {
        delay(task.interval)
        next(null, task)
    }

    val queue = SuspendQueue<TimedTask>(3).apply {
        wait(4000L)
        timeout(5000L)
        process(::job)
        success { println("Success: ${it.name}") }
        failure { err, task -> println("Failure: $err ${task.name}") }
        drain { println("Queue drain") }
        pause()
    }

    println("Start paused")
    for (i in 0 until 10) queue.add(TimedTask("Task$i", i * 1000L))

    delay(3000L)
    println("Resumed")
    queue.resume()

    delay(1000L)
    println("Paused")
    queue.pause()

    delay(1000L)
    println("Resumed")
    queue.resume()

    delay(5000)
}

package ConcurrentQueue

import kotlinx.coroutines.*
import java.lang.Error
import java.time.Duration
import java.time.LocalDateTime

open class TimedTask(
    name: String,
    interval: Long,
    var startTime: LocalDateTime = LocalDateTime.MIN,
    var waitTimeout: Long = Long.MAX_VALUE
): Task(name, interval)  {
    override fun toString(): String = name
}

infix fun TimedTask.isExpiredBy(time: LocalDateTime) = when (waitTimeout) {
    Long.MAX_VALUE -> false
    else -> Duration.between(startTime, time).toMillis() > waitTimeout
}

open class TimeoutQueue<T: TimedTask>(
    concurrency: Int,
    scope: CoroutineScope = GlobalScope
): Queue<T>(concurrency, scope) {
    protected var waitTimeout: Long = Long.MAX_VALUE
    protected var processTimeout: Long = Long.MAX_VALUE
    protected val now: LocalDateTime
        get() = LocalDateTime.now()

    fun wait(msec: Long): TimeoutQueue<T> {
        waitTimeout = msec
        return this
    }

    fun timeout(msec: Long): TimeoutQueue<T> {
        processTimeout = msec
        return this
    }

    override fun add(task: T) {
        if (count < concurrency) {
            next(task)
            return
        }
        task.startTime = now
        task.waitTimeout = waitTimeout
        waiting.add(task)
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
            if (waiting.isNotEmpty()) takeNext()
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

    protected fun shiftTask() = waiting.removeAt(0)

    protected open fun takeNext() {
        val task = shiftTask()

        if (task isExpiredBy now) {
            finish(Error("Waiting timed out"), task)
            if (waiting.isNotEmpty()) takeNext()
            return
        }

        next(task)
    }

    protected fun finish(error: Throwable? = null, task: T) {
        if (error != null) onFailure?.invoke(error, task)
        else onSuccess?.invoke(task)
        onDone?.invoke(error, task)
        if (count == 0) onDrain?.invoke()
    }
}

// Usage

private fun main() = runBlocking {
    suspend fun job(task: TimedTask, next: Errback<TimedTask>) {
        delay(task.interval)
        next(null, task)
    }

    val queue = TimeoutQueue<TimedTask>(3)
        .wait(4000L)
        .timeout(5000L)
        .process(::job)
        .success { println("Success: ${it.name}") }
        .failure { err, task -> println("Failure: $err ${task.name}") }
        .drain { println("Queue drain") }

    for (i in 0 until 10) queue.add(TimedTask("Task$i", i * 1000L))

    delay(10000)
}

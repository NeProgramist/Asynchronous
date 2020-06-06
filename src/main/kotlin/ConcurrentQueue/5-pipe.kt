package ConcurrentQueue

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

open class PipeTask(
    name: String,
    interval: Long,
    startTime: LocalDateTime = LocalDateTime.MIN,
    waitTimeout: Long = Long.MAX_VALUE,
    priority: Int = 0,
    var processed: Boolean = false
): PriorityTask(name, interval, startTime, waitTimeout, priority) {
    override fun toString() = "name = $name; interval = $interval; processed = $processed "
}


class PipeQueue<T: PipeTask>(
    concurrency: Int,
    scope: CoroutineScope = GlobalScope
): PriorityQueue<T>(concurrency, scope) {
    protected var destination: PipeQueue<T>? = null

    override fun finish(error: Throwable?, task: T) {
        if (error != null) {
            onFailure?.invoke(error, task)
        }
        else {
            onSuccess?.invoke(task)
            destination?.add(task)
        }
        onDone?.invoke(error, task)
        if (count == 0) onDrain?.invoke()
    }

    fun pipe(recipient: PipeQueue<T>): PipeQueue<T> {
        destination = recipient
        return this
    }
}

private fun main() = runBlocking {
    val destination = PipeQueue<PipeTask>(2).apply {
        wait(5000)
        process { task, next -> next(null, task.apply { processed = true }) }
        done { error, task -> println("task: $task") }
    }

    val source = PipeQueue<PipeTask>(3).apply {
        timeout(4000)
        process { task, next ->
            delay(task.interval)
            next(null, task)
        }
        pipe(destination)
    }
    for (i in 0 until 10) source.add(PipeTask("Task$i", 1000L))
    delay(15000)
}

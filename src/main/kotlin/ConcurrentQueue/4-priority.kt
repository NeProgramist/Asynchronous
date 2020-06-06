package ConcurrentQueue

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

open class PriorityTask(
    name: String,
    interval: Long,
    startTime: LocalDateTime = LocalDateTime.MIN,
    waitTimeout: Long = Long.MAX_VALUE,
    val priority: Int = 0
): TimedTask(name, interval, startTime, waitTimeout)

open class PriorityQueue<T: PriorityTask>(
    concurrency: Int,
    scope: CoroutineScope = GlobalScope
): TimeoutQueue<T>(concurrency, scope) {
    protected var priorityMode = false

    override fun add(task: T) {
        super.add(task)
        if (priorityMode) {
            waiting.sortByDescending(PriorityTask::priority)
        }
    }

    fun priority(flag: Boolean = true): PriorityQueue<T> {
        priorityMode = flag
        if (flag) waiting.sortByDescending(PriorityTask::priority)
        return this
    }
}


private fun main() = runBlocking {
    suspend fun job(task: PriorityTask, next: Errback<PriorityTask>) {
        delay(task.interval)
        next(null, task)
    }

    val queue = PriorityQueue<PriorityTask>(3).apply {
        process(::job)
        priority()
        done { error, task ->
            if (error != null) println("Done with error: $error")
            else println("Done: $task, count: $count, waiting: ${waiting.size}")
        }
        drain { println("Queue drain") }
    }

    for (i in 0 until 10) queue.add(PriorityTask("Task$i", 1000L, priority = i))
    delay(15000)
}

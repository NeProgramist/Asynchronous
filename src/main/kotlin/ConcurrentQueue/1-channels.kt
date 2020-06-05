package ConcurrentQueue

import kotlinx.coroutines.*

typealias Errback<T> = (Error?, T) -> Unit
open class Task(val name: String, val interval: Long)

open class Queue<T: Task>(
    private val concurrency: Int,
    private val scope: CoroutineScope = GlobalScope
) {
    var count = 0
    val waiting = mutableListOf<T>()
    private var onDone: ((Throwable?, T) -> Unit)? = null
    private var onDrain: (() -> Unit)? = null
    private var onProcess: (suspend (T, Errback<T>) -> Unit)? = null
    private var onSuccess: ((T) -> Unit)? = null
    private var onFailure: ((Throwable) -> Unit)? = null

    open fun add(task: T) {
        if (count < concurrency) {
            next(task)
            return
        }
        waiting.add(task)
    }

    open fun next(task: T) {
        count++
        scope.launch {
            onProcess?.invoke(task) { error, task ->
                if (error != null) onFailure?.invoke(error)
                else onSuccess?.invoke(task)

                onDone?.invoke(error, task)
                count--

                if (waiting.size > 0) {
                    next(waiting.removeAt(0))
                    return@invoke
                }
                if (count == 0) onDrain?.invoke()
            }
        }
    }

    fun process(listener: suspend (T, Errback<T>) -> Unit): Queue<T> {
        onProcess = listener
        return this
    }

    fun done(listener: (Throwable?, T) -> Unit): Queue<T> {
        onDone = listener
        return this
    }

    fun success(listener: (T) -> Unit): Queue<T> {
        onSuccess = listener
        return this
    }

    fun failure(listener: (Throwable) -> Unit): Queue<T> {
        onFailure = listener
        return this
    }

    fun drain(listener: () -> Unit): Queue<T> {
        onDrain = listener
        return this
    }
}

private fun main() = runBlocking {
    suspend fun job(task: Task, next: Errback<Task>) {
        println("Process: ${task.name}")
        delay(task.interval)
        next(null, task)
    }

    val queue = Queue<Task>(3).apply {
        process(::job)
        done { error, task ->
            if (error != null) println("Done with error: $error")
            else println("Done: ${task.name}, count: $count, waiting: ${waiting.size}")
        }
        success { println("Success: ${it.name}") }
        failure { println("Failure: $it") }
        drain { println("Queue drain") }
    }

    for (i in 0 until 10) queue.add(Task("Task$i", i*100L))
    delay(15000)
}

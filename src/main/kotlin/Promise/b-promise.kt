package Promise

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList

open class Promise<T>(
    private val scope: CoroutineScope = GlobalScope,
    cb: suspend (resolve: (T) -> Unit, reject: (Throwable) -> Unit) -> Unit
) {
    private var data: T? = null
    private var error: Throwable? = null
    private var status: Status = Status.PENDING
    private val resolvers = mutableListOf<suspend (T) -> Unit>()
    private val catchers = mutableListOf<suspend (Throwable) -> Unit>()
    private val finalizers = mutableListOf<suspend (Pair<T?, Throwable?>) -> Unit>()
    private val job = scope.launch { cb(this@Promise::resolve, this@Promise::reject) }

    fun resolve(value: T) {
        if (status != Status.PENDING) return
        synchronized(this) {
            if (status != Status.PENDING) return
            status = Status.RESOLVED
            data = value
        }
        resolvers.forEach {
            runResolver(it)
        }
        finalizers.forEach {
            runFinalizer(it)
        }
    }

    fun reject(reason: Throwable) {
        if (status != Status.PENDING) return
        synchronized(this) {
            if (status != Status.PENDING) return
            if (job.isActive) job.cancel()
            status = Status.REJECTED
            error = reason
        }
        catchers.forEach {
            runCatcher(it)
        }
        finalizers.forEach {
            runFinalizer(it)
        }
    }

    fun then(f: suspend (T) -> Unit): Promise<T> {
        when (status) {
            Status.RESOLVED -> runResolver(f)
            Status.PENDING -> resolvers.add(f)
            Status.REJECTED -> Unit
        }
        return this
    }

    fun <K> after(f: suspend (T) -> K): Promise<K> = Promise { resolve, reject ->
        then { data ->
            try {
                resolve(f(data))
            } catch (e: Throwable) {
                reject(e)
            }
        }
    }

    fun catch(f: suspend (Throwable) -> Unit): Promise<T> {
        when (status) {
            Status.RESOLVED -> Unit
            Status.REJECTED -> runCatcher(f)
            Status.PENDING -> catchers.add(f)
        }
        return this
    }

    fun finally(f: suspend (Pair<T?, Throwable?>) -> Unit): Promise<T> {
        when (status) {
            Status.PENDING -> finalizers.add(f)
            else -> runFinalizer(f)
        }
        return this
    }

    suspend fun await() {
        val signal = Channel<Boolean>()
        finally {
            signal.send(true)
        }
        signal.receive()
    }

    override fun toString() = when(status) {
        Status.RESOLVED -> "[Promise: Resolved { $data }]"
        Status.REJECTED -> "[Promise: Rejected { $error}]"
        Status.PENDING -> "[Promise: Pending]"
    }

    private fun runResolver(f: suspend (T) -> Unit) = scope.launch { data?.let { f(it) } }
    private fun runCatcher(f: suspend (Throwable) -> Unit) = scope.launch { error?.let { f(it) } }
    private fun runFinalizer(f: suspend (Pair<T?, Throwable?>) -> Unit) = scope.launch {
        f(data to error)
    }

    enum class Status {
        RESOLVED,
        PENDING,
        REJECTED,
    }

    companion object {
        fun <T> resolve(value: T) = Promise<T> { _, _ -> }.apply { resolve(value) }
        fun reject(error: Error) = Promise<Nothing> { _, _ -> }.apply { reject(error) }

        private fun Array<out Promise<*>>.rejectAll(error: Throwable, cb: () -> Unit) {
            cb()
            forEach { it.reject(error) }
        }

        fun <T> all(vararg promises: Promise<T>) = Promise<List<T>> { resolve, reject ->
            val channel = Channel<T>(promises.size)

            promises.forEach {
                it.then(channel::send)
                it.catch { error -> promises.rejectAll(error) {
                    channel.cancel()
                    reject(error)
                }}
            }

            resolve(List(promises.size) {
                channel.receive()
            })
        }

        fun <T> race(vararg promises: Promise<T>) = Promise<T> { resolve, reject ->
            val channel = Channel<T>()

            promises.forEach {
                it.then(channel::send)
                it.catch { error -> promises.rejectAll(error) {
                    channel.cancel()
                    reject(error)
                }}
            }

            val res = channel.receive()
            promises.forEach {
                it.reject(Error("Lost race"))
            }
            resolve(res)
        }
    }
}

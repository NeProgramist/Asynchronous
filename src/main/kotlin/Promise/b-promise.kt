package Promise

import kotlinx.coroutines.*

open class Promise<T>(
    private val scope: CoroutineScope = GlobalScope,
    cb: suspend (resolve: (T) -> Unit, reject: (Throwable) -> Unit) -> Unit
) {
    private var data: T? = null
    private var error: Throwable? = null
    private var status: Status = Status.PENDING
    private val resolvers = mutableListOf<suspend (T) -> Unit>()
    private val catchers = mutableListOf<suspend (Throwable) -> Unit>()
    private val job = scope.launch { cb(this@Promise::resolve, this@Promise::reject) }

    fun resolve(value: T) {
        if (status != Status.PENDING) return
        status = Status.RESOLVED
        data = value
        resolvers.forEach {
            runResolver(it)
        }
    }

    fun reject(reason: Throwable) {
        if (status != Status.PENDING) return
        if (job.isActive) job.cancel()
        status = Status.REJECTED
        error = reason
        catchers.forEach {
            runCatcher(it)
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

    fun <K> after(f: (T) -> Promise<K>): Promise<K> = Promise { resolve, reject ->
        then { data -> f(data)
            .then { resolve(it) }
            .catch { reject(it)  }
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

    override fun toString() = when(status) {
        Status.RESOLVED -> "[Promise: Resolved { $data }]"
        Status.REJECTED -> "[Promise: Rejected { $error}]"
        Status.PENDING -> "[Promise: Pending]"
    }

    private fun runResolver(f: suspend (T) -> Unit) = scope.launch { data?.let { f(it) } }
    private fun runCatcher(f: suspend (Throwable) -> Unit) = scope.launch { error?.let { f(it) } }

    enum class Status {
        RESOLVED,
        PENDING,
        REJECTED,
    }

    companion object {
        fun <T> resolve(value: T) = Promise<T> { _, _ -> }.apply { resolve(value) }
        fun reject(error: Error) = Promise<Nothing> { _, _ -> }.apply { reject(error) }

        fun <T> all(vararg promises: Promise<T>) = Promise<List<T>> { resolve, reject ->
            val results = mutableListOf<T>()
            val count = promises.size

            suspend fun resolveOne(data: T) = results.let {
                it.add(data)
                if (it.size == count) resolve(it)
            }

            suspend fun rejectOne(error: Throwable) {
                reject(error)
                promises.forEach { it.reject(error) }
            }

            promises.forEach { it.then(::resolveOne).catch(::rejectOne) }
        }

        fun <T> race(vararg promises: Promise<T>) = Promise<T> { resolve, reject ->
            suspend fun resolveOne(data: T) {
                resolve(data)
                promises.forEach { it.reject(Error("Lost race")) }
            }
            suspend fun rejectAll(reason: Throwable) {
                reject(reason)
                promises.forEach { it.reject(reason) }
            }

            promises.forEach { it.then(::resolveOne).catch(::rejectAll) }
        }
    }
}

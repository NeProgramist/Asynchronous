package EventEmitter

import kotlinx.coroutines.*

class AsyncEmitter<T>(private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)) {
    private val events = mutableMapOf<String, Events<T>>()

    private fun event(name: String) = events.getOrPut(name) { Events() }
    fun on(name: String, func: suspend (T) -> Unit) = event(name).on.add(func)
    fun once(name: String, func: suspend (T) -> Unit) = event(name).once.add(func)

    fun emit(name: String, data: T) {
        val event = events[name] ?: return

        event.on.forEach { scope.launch { it(data) } }
        event.once.run {
            forEach { scope.launch { it(data) } }
            clear()
        }
        if (event.count() == 0) events.remove(name)
    }

    fun remove(name: String, func: suspend (T) -> Unit) {
        val event = events[name] ?: return
        event.on.remove(func)
        event.once.remove(func)
        if (event.count() == 0) events.remove(name)
    }

    fun clear(name: String? = null) = name?.let { events.remove(it) } ?: events.clear()
    fun count(name: String) = events[name]?.count()
    fun listeners(name: String) = events[name]?.listeners()

    val names: Set<String>
        get() = events.keys

    private data class Events<T>(
        val on: MutableList<suspend (T) -> Unit> = mutableListOf(),
        val once: MutableList<suspend (T) -> Unit> = mutableListOf()
    ) {
        fun count() = on.size + once.size
        fun listeners() = (on + once).toList()
    }
}

suspend fun main() {
    val ee = AsyncEmitter<String>(CoroutineScope(Dispatchers.Unconfined))

    // on and emit
    ee.on("e1") {
        delay(2000)
        println(it) }
    ee.emit("e1", "e1 ok")

    // once
    ee.once("e2") {
        delay(1000)
        println(it)
    }
    ee.emit("e2", "e2 ok")
    ee.emit("e2", "e2 not ok")

    // remove
    suspend fun f3(data:String) {
        delay(500)
        println(data)
    }

    ee.on("e3",  ::f3)
    ee.remove("e3", ::f3)
    ee.emit("e3", "e3 not ok")

    // count
    ee.on("e4") {}
    ee.on("e4") {}
    println("e4 count: " + ee.count("e4"))

    // clear
    ee.clear("e4")
    ee.emit("e4", "e4 not ok")
    ee.emit("e1", "e1 ok")

    ee.clear()
    ee.emit("e1", "e1 ok")

    // listeners and names
    ee.on("e5") {}
    ee.on("e5") {}
    ee.on("e6") {}
    ee.on("e7") {}

    println("listeners: " + ee.listeners("e5"))
    println("names: " + ee.names)

    delay(3000)
}

package EventEmitter

import kotlinx.coroutines.*

private class EventEmitter2<K> {
    private val events = mutableMapOf<String, MutableList<(K) -> Unit>>()
    private val wrappers = mutableMapOf<(K) -> Unit, (K) -> Unit>()
    private val garbage = mutableListOf<(K) -> Unit>()

    private fun clearGarbage(name: String) {
        val event = events[name] ?: return
        garbage.forEach { event.remove(it) }
    }

    fun on(name: String, timeout: Long = 0, f: (K) -> Unit) {
        events.getOrPut(name) { mutableListOf() }.add(f)
        if (timeout > 0.0) GlobalScope.launch {
            delay(timeout)
            remove(name, f)
        }
    }

    fun emit(name: String, data: K) = events[name]?.forEach { it(data) }.also { clearGarbage(name) }

    fun once(name: String, timeout: Long = 0, f: (K) -> Unit) {
        lateinit var wrapper: (K) -> Unit
        wrapper = {
            garbage += wrapper
            f(it)
        }

        wrappers += f to wrapper
        on(name, timeout, wrapper)
    }

    fun remove(name: String, f: (K) -> Unit) {
        events[name]?.remove(f)
        wrappers[f]?.let { events[name]?.remove(it) }
        wrappers -= f
    }

    fun clear(name: String? = null) = name?.let { events.remove(it) } ?: events.clear()
    fun count(name: String) = events[name]?.size ?: 0
    fun listeners(name: String) = events[name]?.run { subList(0, size) }

    val names: Set<String>
        get() = events.keys
}

// Usage
fun main() {
    val ee = EventEmitter2<String>()

    // on and emit
    ee.on("e1") { println(it) }
    ee.emit("e1", "e1 ok")

    // once
    ee.once("e2") { println(it) }
    ee.emit("e2", "e2 ok")
    ee.emit("e2", "e2 not ok")

    // remove
    val f3 = { data: String ->
        println(data)
    }

    ee.on("e3", 0, f3)
    ee.remove("e3", f3)
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
}

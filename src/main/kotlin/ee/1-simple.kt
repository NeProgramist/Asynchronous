package ee

fun main() {
    val ee = EventEmitter<String>()
    ee.on("first") { data -> println("first data - $data") }
    ee.on("first") { data -> println("second data - $data") }

    ee.emit("first", "i'm here")
}

class EventEmitter<T> {
    private val events = mutableMapOf<String, MutableList<(T) -> Unit>>()

    fun on(name: String, func: (T) -> Unit) {
        if (events[name] == null) events[name] = mutableListOf(func)
        else events[name]?.add(func)
    }

    fun emit(name: String, data: T) = events[name]?.forEach { it(data) }
}


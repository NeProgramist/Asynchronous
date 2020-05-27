package EventEmitter

interface Emitter<K> {
    fun on(name: String, fn: (K) -> Unit): Boolean
    fun emit(name: String, data: K)
}

private fun <K> emitter(): Emitter<K> {
    val events = mutableMapOf<String, MutableList<(K) -> Unit>>()

    return object: Emitter<K> {
        override fun on(name: String, fn: (K) -> Unit) = events
        .getOrPut(name) { mutableListOf() }
        .add(fn)

        override fun emit(name: String, data: K) = events
            .getOrDefault(name, null)
            ?.forEach { it(data) } ?: Unit
    }
}

// Usage
fun main() {
    val ee = emitter<String>()
    ee.on("event1") { println("RECEIVED: $it") }
    ee.emit("event1", "data")
}

package AsynchronousProgramming

import kotlinx.coroutines.*
typealias Errback<T> = (Error?, T) -> Unit

// Emulate asynchronous calls

fun <T: Any, K: Any> wrapAsync(fn: (T, Errback<K>) -> Unit) = { args: T, cb: Errback<K> ->
    GlobalScope.launch {
        delay((Math.random() * 1000).toLong())
        fn(args, cb)
    }
}

// Asynchronous functions

val readConfig = wrapAsync { name: String, cb: Errback<String> ->
    println("(1) config loaded")
    cb(null, name)
}

val doQuery = wrapAsync { statement: String, cb: Errback<Map<String, String>> ->
    println("(2) SQL query executed: $statement")
    cb(null, mapOf("city" to "Kyiv", "univ" to "KPI"))
}

val httpGet = wrapAsync { url: String, cb: Errback<String> ->
    println("(3) Page retrieved: $url")
    cb(null, "<html>Some archaic web here</html>")
}

val readFile = wrapAsync { path: String, cb: Errback<String> ->
    println("(4) File '$path' loaded")
    cb(null, "file content")
}

fun callbackCounter(count: Int, cb: () -> Unit): () -> Unit {
    var counter = 0
    return { if (++counter == count) cb() }
}

val callback = callbackCounter(4) { println("All done!") }

private fun main() = runBlocking {
    println("start")

    val job1 = readConfig("myConfig") { _, _ -> callback() }
    val job2 = doQuery("select * from cities") { _, _ -> callback() }
    val job3 = httpGet("http://kpi.ua") { _, _ -> callback() }
    val job4 = readFile("2-emulate-async-calls.kt") { _, _ -> callback() }

    joinAll(job1, job2, job3, job4)

    println("end")
}

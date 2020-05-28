package AsynchronousProgramming

import kotlinx.coroutines.*

typealias Suspback<T> = suspend (Error?, T) -> Unit

private fun wrapAsync(fn: suspend (Any, Suspback<Any>) -> Unit) = { args: Any, cb: Suspback<Any> ->
    GlobalScope.launch {
        delay((Math.random() * 1000).toLong())
        fn(args, cb)
    }
}

private val readConfig = wrapAsync { name, cb ->
    println("(1) config loaded")
    cb(null, name)
}

private val doQuery = wrapAsync { statement, cb ->
    println("(2) SQL query executed: $statement")
    cb(null, mapOf("city" to "Kyiv", "univ" to "KPI"))
}

private val httpGet = wrapAsync { url, cb ->
    println("(3) Page retrieved: $url")
    cb(null, "<html>Some archaic web here</html>")
}

private val readFile = wrapAsync { path, cb ->
    println("(4) File '$path' loaded")
    cb(null, "file content")
}

private suspend fun main() {
    readConfig("myConfig") { _, _ ->
        doQuery("select * from cities") { _, _ ->
            httpGet("http://kpi.ua") { _, _ ->
                readFile("README.md") { _, _ ->
                    println("All done")
                }.join()
            }.join()
        }.join()
    }.join()
}

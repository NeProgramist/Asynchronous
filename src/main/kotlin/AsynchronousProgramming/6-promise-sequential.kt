package AsynchronousProgramming

import kotlinx.coroutines.*
import java.util.*

// Emulate asynchronous calls

private fun <K, T> wrapAsync(fn: suspend (K) -> T): suspend (K) -> Deferred<T> = {
    GlobalScope.async {
        delay((Math.random() * 1000).toLong())
        fn(it)
    }
}

private fun isWeekend() = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) in listOf(1, 7)

private val readConfig = wrapAsync { name: String ->
    println("(1) config loaded")
    if (!isWeekend()) return@wrapAsync name
    else throw Error("Deferred will resolve next working day")
}

private val doQuery = wrapAsync { statement: String ->
    println("(2) SQL query executed: $statement")
    if (!isWeekend()) return@wrapAsync listOf("Kyiv", "Roma")
    else throw Error("Deferred will resolve next working day")
}

private val httpGet = wrapAsync { url: String ->
    println("(3) Page retrieved: $url")
    if (!isWeekend()) return@wrapAsync "<html>Some archaic web here</html>"
    else throw Error("Deferred will resolve next working day")
}


private val readFile = wrapAsync { path: String ->
    println("(4) File loaded: $path")
    if (!isWeekend()) return@wrapAsync "file content"
    else throw Error("Deferred will resolve next working day")
}

private fun main() = runBlocking {
    println("Start")

    val res1 = readConfig("myConfig").await()
    val res2 = doQuery("select * from cities").await()
    val res3 = httpGet("http://kpi.ua").await()
    val res4 = readFile("README.md").await()

    println("Res: $res1\n\t$res2\n\t$res3\n\t$res4")
    println("Done")
}

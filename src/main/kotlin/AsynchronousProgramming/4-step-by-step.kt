package AsynchronousProgramming

import kotlinx.coroutines.*

// Back to order
// Use global data and decentralized control flow (bad practices)
val data = mutableMapOf<String, Any>()

private fun wrapAsync(fn: suspend () -> Unit): suspend () -> Job = {
    GlobalScope.launch {
        delay((Math.random() * 1000).toLong())
        fn()
    }
}

// Asynchronous functions
private val readFile = wrapAsync {
    println("(4) Readme file loaded")
    data += "readme" to "file content"
    println(data)
    println("All done!")
}

private val getHttpPage = wrapAsync {
    println("(3) Page retrieved")
    data += "html" to "<html>Some archaic web here</html>"
    readFile().join()
}

private val selectFromDb = wrapAsync {
    println("(2) SQL query executed")
    data += "cities" to listOf("Kyiv", "Roma")
    getHttpPage().join()
}

private val readConfig = wrapAsync {
    println("(1) config loaded")
    data += "config" to mapOf("name" to "Karl Marx")
    selectFromDb().join()
}

private fun main() = runBlocking {
    println("start")
    readConfig().join()
    println("end")
}

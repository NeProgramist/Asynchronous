package Promise

import kotlinx.coroutines.*

import java.io.File

private fun main() = runBlocking {
    // Active
    val deferred1 = async { "value1" }
    println(deferred1) // DeferredCoroutine{Active}@7b1d7fff
    println(deferred1.await()) // value1

    // Immediate result from coroutine
    println()
    val deferred2 = withContext(Dispatchers.Default) { "value2" }
    println(deferred2) // value2

    // Immediate error from coroutine
    println()
    try {
        withContext<Nothing>(Dispatchers.Default) { throw Error("I have no value for you") }
    }
    catch (e: Error) {
        println(e) // java.lang.Error: I have no value for you
    }

    // Deferred.await()
    println()
    val deferred3 = async { "value3" }
    println(deferred3) // DeferredCoroutine{Active}@591f989e
    println(deferred3.await()) // value3

    // Catch coroutine exception
    println()
    val deferred4: Deferred<String> = GlobalScope.async {
        throw Error("I have no value for you")
        "value 4"
    }
    println(deferred4) // DeferredCoroutine{Active}@61443d8f
    try {
        println(deferred4.await())
    } catch (e: Error) {
        println(e) // java.lang.Error: I have no value for you
    }

    // Example with I/O


    val file1 = GlobalScope.async {
        File("$FILE_PATH/file1.txt").readText()
    }
    val file2 = GlobalScope.async {
        File("$FILE_PATH/file2.txt").readText()
    }
    val file3 = GlobalScope.async {
        File("$FILE_PATH/file3.txt").readText()
    }

    println("file1 = $file1")
    println("file2 = $file2")
    println("file3 = $file3")
    println("Waited:")
    println("${file1.await()}, file1 = $file1")
    println("${file2.await()}, file2 = $file1")
    println("${file3.await()}, file3 = $file1")

    return@runBlocking
}
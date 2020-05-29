package Promise

import kotlinx.coroutines.*

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



    return@runBlocking
}
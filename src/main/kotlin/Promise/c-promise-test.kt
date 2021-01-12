package Promise

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread

private fun testGeneral() = runBlocking {
    val promise = Promise<String> { resolve, _ ->
        delay(5000)
        resolve("Hello, world!")
    }.then {
        delay(500)
        println("I've resolved with: $it")
    }.then {
        println("Another one then: $it")
    }.catch {
        println("I've caught $it")
    }

//    promise.reject(Error("Rejected"))

    println(promise)
    promise.await()
    println(promise)
}

private fun testImmediate() {
    println("\nImmediate: ")
    println(Promise.resolve(5))
    println(Promise.reject(Error("Already rejected")))
}

private fun testAfter() = runBlocking {
    val promise = Promise<String> { resolve, reject ->
        try {
            File("./src/main/kotlin/Promise/c-promise-test.kt").readText().also(resolve)
        } catch (e: IOException) {
            reject(e)
        }
    }
    promise.catch { println(it.message) }
    promise.after { code ->
        println(code)
        delay(1000L)
        code.split(' ').count { w -> w.contains("promise", ignoreCase = true) }
    }.after { number ->
        println("Number of words \"promise\" in code: $number")
        delay(600L)
        if (number < 20) error("Not enough promises")
        else true
    }.then {
        println("Enough promises: $it")
    }.catch {
        println("Error: ${it.message}")
    }.finally {
        println("Promise is over")
    }.await()
}

fun testAll() = runBlocking {
    val promises = Array(10) {
        Promise<Int> { resolve, reject ->
            delay(100L)
//            if (it == 9) reject(Error("Must reject"))
            resolve(it)
        }
    }
    Promise.all(*promises)
        .then {
            println("Everybody resolved: $it")
        }.catch {
            println("At least one rejected: ${it.message}")
        }.await()
}

fun testRace() = runBlocking {
    val promises = Array(10) {
        Promise<Int> { resolve, reject ->
            if (it % 2 != 0) {
//                delay(100L)
                reject(Error("Must reject"))
            } else {
//                delay(500L)
                resolve(it)
            }
        }
    }

    Promise.race(*promises)
        .then {
            println("First resolved: $it")
        }.catch {
            println("First rejected: ${it.message}")
        }.await()

}

fun testMultithreading() = runBlocking {
    Promise<Int> { resolve, reject ->
        List(4) {
            thread {
                Thread.sleep(1000L)
                resolve(it)
            }
        }.forEach(Thread::run)
    }.then {
        println("Resolved: $it")
    }.await()
}

// Usage
private fun main() {
    testGeneral()
    testImmediate()
    testAfter()
    testAll()
    testRace()
    testMultithreading()
}

package Promise

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException

private fun testGeneral() = runBlocking {
    val promise = Promise<String> { resolve, _ ->
        delay(5000)
        resolve("Hello, world!")
    }.then {
        delay(500)
        println("I've resolved with: $it")
    }
        .then { println("Another one then: $it") }
        .catch { println("I've caught $it") }

//    promise.reject(Error("Rejected"))

    println(promise)
    delay(6000)
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
        Promise<Int> { resolve, _ ->
            delay(1000L)
            val n = code
                .split(' ')
                .count { word -> word.contains("promise", ignoreCase = true) }
            resolve(n)
        }
    }.after { number ->
        println("Number of words \"promise\" in code: $number")
        Promise<Boolean> { resolve, reject ->
            delay(600)
            if (number < 20) {
                reject(Exception("Not enough promises"))
            } else resolve(true)
        }
    }.then {
        println("Enough promises: $it")
    }.catch {
        println(it.message)
    }

    delay(2001L)
}

// Usage
private fun main() {
    testGeneral()
    testImmediate()
    testAfter()
}

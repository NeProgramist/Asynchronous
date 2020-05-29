package Promise

import AsynchronousProgramming.Errback
import kotlinx.coroutines.*
import java.io.File


@Suppress("UNCHECKED_CAST")
class Thenable<T> {
    private var next: Thenable<T>? = null
    private var func: ((T) -> Any)? = null

    fun then(f: (T) -> Any): Thenable<T> {
        func = f
        next = Thenable()
        return next ?: error("next is null(it'll never happen)")
    }

    fun resolve(value: T) {
        println("fun $func")
        val next = func?.let{ it(value) }
        println("next $next")
        if (next is Thenable<*>) next.then {
            this.next?.resolve(it as T)
            Unit
        }
    }
}

fun main() = runBlocking {
    fun readFile(filename: String): Thenable<String> {
        val thenable = Thenable<String>()
        GlobalScope.launch {
            val text = File("$FILE_PATH/$filename").readText()
            println("text - $text")
            thenable.resolve(text)
            println("fuck")
        }
        println("out")
        return thenable
    }

    readFile("file1.txt")
        .then {
            println("file1: $it")
            return@then readFile("file2.txt")
        }.then {
            println("file2: $it")
            return@then readFile("file3.txt")
        }.then {
            println("file3: $it")
        }

    delay(10000)
    return@runBlocking
}
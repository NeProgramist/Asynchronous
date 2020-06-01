package Promise

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
        val n = func?.let{ it(value) }
        if (n is Thenable<*>) n.then {
            next?.resolve(it as T)
            Unit
        }
    }
}

fun main() = runBlocking {
    fun readFile(filename: String): Thenable<String> {
        val thenable = Thenable<String>()
        launch {
            val text = File("$FILE_PATH/$filename").readText()
            thenable.resolve(text)
        }
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

    joinAll()
    return@runBlocking
}

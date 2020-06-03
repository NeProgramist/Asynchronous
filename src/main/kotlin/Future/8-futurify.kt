package Future

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

typealias Errback<T> = (Error?, T) -> Unit

class FuturifyClass<T>(
    private val executor: (resolve: (T) -> Unit, reject: (Error) -> Unit) -> Unit
) {
    companion object {
        fun <T> of(value: T) = FuturifyClass<T> { resolve, _ -> resolve(value) }
    }

    fun chain(fn: (T) -> FuturifyClass<T>) = FuturifyClass<T> { resolve, reject -> fork(
        { fn(it).fork(resolve, reject) },
        { reject(it) }
    )}

    fun map(fn: (T) -> T) = chain { of(fn(it)) }

    fun fork(succeeded: (T) -> Unit, failed: (Error) -> Unit) {
        executor(succeeded, failed)
    }
}

fun <T> futurify(fn: (T, Errback<T>) -> Unit) = { arg: T ->
    FuturifyClass<T> { resolve, reject ->
        fn(arg) { error, data ->
            if(error != null) reject(error)
            else resolve(data)
        }
    }
}

private suspend fun main() {
    val readFile = { name: String, callback: Errback<String> ->
        GlobalScope.launch {
            val file = File(name).readText()
            callback(null, file)
        }
        Unit
    }

    val futureFile = futurify(readFile)

    futureFile("src/main/kotlin/Future/8-futurify.kt")
        .map {
            println("length: ${it.length}")
            it
        }
        .fork(
            { println("file: $it") },
            { println("error: $it") }
        )

    delay(100)
}

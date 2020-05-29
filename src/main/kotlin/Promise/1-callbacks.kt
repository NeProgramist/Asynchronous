package Promise

import kotlinx.coroutines.*
import java.io.File

fun main() = runBlocking {
    launch {
        withContext(Dispatchers.IO) {
            File("src/main/kotlin/Promise/file1.txt").readText().also {println(it)}
            withContext(Dispatchers.IO) {
                File("src/main/kotlin/Promise/file2.txt").readText().also {println(it)}
                withContext(Dispatchers.IO) {
                    File("src/main/kotlin/Promise/file3.txt").readText().also {println(it)}
                }
            }
        }
    }
    println("End reading from file")
}
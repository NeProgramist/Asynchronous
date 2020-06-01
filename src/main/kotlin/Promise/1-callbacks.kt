package Promise

import kotlinx.coroutines.*
import java.io.File

fun main() = runBlocking {
    launch {
        withContext(Dispatchers.IO) {
            File("$FILE_PATH/file1.txt").readText().also {println(it)}
            withContext(Dispatchers.IO) {
                File("$FILE_PATH/file2.txt").readText().also {println(it)}
                withContext(Dispatchers.IO) {
                    File("$FILE_PATH/file3.txt").readText().also {println(it)}
                }
            }
        }
    }
    println("End reading from file")
}

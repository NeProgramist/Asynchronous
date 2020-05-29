package Promise

import kotlinx.coroutines.runBlocking

private fun main() = runBlocking {
    val content = fetchAsync("https://www.google.com")
    try {
        println(content.await())
    } catch (e: Exception) {
        println(e)
    }
}

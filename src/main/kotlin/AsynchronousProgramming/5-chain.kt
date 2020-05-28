package AsynchronousProgramming

import kotlinx.coroutines.*

interface Chain<T> {
    val prev: Chain<T>?
    var next: Chain<T>?
    operator fun invoke()
    fun act(fn: (T, Errback<T>) -> Unit, args: T): Chain<T>
    fun forward()
}

fun <T: Any> chain(prev: Chain<T>? = null): Chain<T> = object: Chain<T> {
    override val prev = prev
    override var next: Chain<T>? = null
    private var fn: ((T, Errback<T>) -> Unit)? = null
    private lateinit var args: T

    override fun invoke() {
        println("Reverse from $fn")
        prev?.let {
            if (fn != null) prev.next = this
            prev()
        } ?: forward()
    }

    override fun act(fn: (T, Errback<T>) -> Unit, args: T): Chain<T> {
        this.fn = fn
        this.args = args
        return chain(this)
    }

    override fun forward() {
        println("Forward")
        fn?.let {
            it(args) {err, data ->
                println("Data: $data")
                println("Callback from $fn")
                if (err == null && next != null) next?.forward()
                else println("End at $fn")
            }
        }
    }
}


private fun <T, K> wrapAsync(fn: (T, Errback<K>) -> Unit) = { args: T, cb: Errback<K> ->
    GlobalScope.launch {
        delay((Math.random() * 1000).toLong())
        fn(args, cb)
    }
    Unit
}

// Asynchronous functions

private val readConfig = wrapAsync { name: String, cb: Errback<String> ->
    println("(1) config loaded")
    cb(null, name)
}

private val doQuery = wrapAsync { statement: String, cb: Errback<String> ->
    println("(2) SQL query executed: $statement")
    cb(null, "[{ name: 'Kiev' }, { name: 'Roma' } ]")
}

private val httpGet = wrapAsync { url: String, cb: Errback<String> ->
    println("(3) Page retrieved: $url")
    cb(null, "<html>Some archaic web here</html>")
}

private val readFile = wrapAsync { path: String, cb: Errback<String> ->
    println("(4) File '$path' loaded")
    cb(null, "file content")
}
private suspend fun main() {
    val chain = chain<String>()
        .act(readConfig, "myConfig")
        .act(doQuery, "select * from cities")
        .act(httpGet, "http://kpi.ua")
        .act(readFile, "README.md")

    chain()
    delay(10000)
}
package AsynchronousProgramming

import kotlinx.coroutines.delay

private suspend fun readConfig(name: String, callback: (err: Error?, data: String) -> Unit) {
        delay(1000)
        println("(1) config loaded: $name")
        callback(null, name)
}

private suspend fun doQuery(statement: String, callback: (err: Error?, data: String) -> Unit) {
    delay(1000)
    println("(2) SQL query executed: $statement")
    callback(null, "[{ name: 'Kiev' }, { name: 'Roma' }]")
}


private suspend fun httpGet(url: String, callback: (err: Error?, data: String) -> Unit) {
    delay(1000)
    println("(3) Page retrieved: $url")
    callback(null, "<html>Some archaic web here</html>")
}


private suspend fun readFile(path: String, callback: (err: Error?, data: String) -> Unit) {
    delay(1000)
    println("(4) Readme file loaded")
    callback(null, "file loaded")
}


private suspend fun main() {
    val callback = {er: Error?, data:String -> }

    println("start")
    readConfig("myConfig", callback);
    doQuery("select * from cities", callback);
    httpGet("http://kpi.ua", callback);
    readFile("README.md", callback);
    println("end")

    delay(2000)
}

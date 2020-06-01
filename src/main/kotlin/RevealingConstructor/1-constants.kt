package RevealingConstructor

const val scalarConstant = 5
val functionConstant = { 6 }
val callbackConstant = { func: (Int) -> Unit -> func(7) }

fun fn(x: Int, f: () -> Int, g: ((Int) -> Unit) -> Unit) {
    println("constant: $x")
    println("function: ${f()}")
    g { println("callback: $it") }
}

fun main() {
    fn(scalarConstant, functionConstant, callbackConstant)
}

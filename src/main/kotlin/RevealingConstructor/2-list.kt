package RevealingConstructor

// Standard Kotlin List constructor
private fun main() {
    val list = List(10) { it * 2 }
    println(list) // [0, 2, 4, 6, 8, 10, 12, 14, 16, 18]
}

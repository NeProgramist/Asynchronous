package Future

import kotlin.math.pow

private fun main() {
    val f1 = FutureClass.of(6.0)
    val f2 = f1.map { it + 1.0 }
    val f3 = f2.map { it.pow(3) }
    val f4 = f1.map { it * 2 }

    f1.fork({ println("f1 fork1: $it") })
    f1.fork({ println("f1 fork2: $it") })
    f2.fork({ println("f2 fork1: $it") })
    f2.fork({ println("f2 fork2: $it") })
    f3.fork({ println("f3 fork1: $it") })
    f3.fork({ println("f3 fork2: $it") })
    f4.fork({ println("f4 fork1: $it") })
    f4.fork({ println("f4 fork2: $it") })

    println("\nChange initial value of chain:")
    ({
        var source = 2.0
        val l = { r: (Double) -> Unit, _: (Error) -> Unit -> r(source) }
        val f1 = FutureClass(l)
            .map { it + 1 }
            .map { it.pow(3) }
            .map { it * 2 }
        f1.fork({ println("fork1: $it") })
        source = 3.0
        f1.fork({ println("fork2: $it") })
        source = 4.0
        f1.fork({ println("fork3: $it") })
        source = 5.0
        f1.fork({ println("fork5: $it") })
    })()
}

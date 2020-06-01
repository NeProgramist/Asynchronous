package Future

interface Future<T> {
    fun map(fn: (T) -> T): Future<T>
    fun fork(fn: (T) -> Unit): Future<T>
}

private fun <T> future(value: T): Future {
    val mapper: (T) -> T
    return object: Future {
        override fun map() {
        }

        override fun fork() {
        }

        override fun chain() {}
    }
}

private fun main() {

}
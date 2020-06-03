package Future

interface FailableFuture<T> {
    fun map(fn: (T) -> T): FailableFuture<T>
    fun fork(succeeded: (T) -> Unit, failed: (Throwable) -> Unit): FailableFuture<T>
    fun chain(fn: (T) -> FailableFuture<T>): FailableFuture<T>

}

private fun <T> future(
    executor: (resolve: (T) -> Unit, reject: (Throwable) -> Unit) -> Unit
) = object: FailableFuture<T> {
    override fun chain(fn: (T) -> FailableFuture<T>) = this
    //future { resolve: (T) -> Unit, reject: (Throwable) -> Unit ->
    //     fork(
    //         { value -> fn(value).fork(resolve, reject) },
    //         { error -> reject(error) }
    //     )
    // }

    // type checking has run into a recursive problem

    override fun map(fn: (T) -> T) = chain { value -> futureOf(fn(value)) }

    override fun fork(succeeded: (T) -> Unit, failed: (Throwable) -> Unit): FailableFuture<T> {
        executor(succeeded, failed)
        return this
    }
}

private fun <T> futureOf(value: T) = future<T> { resolve, _ ->  resolve(value) }

private fun main() {

}

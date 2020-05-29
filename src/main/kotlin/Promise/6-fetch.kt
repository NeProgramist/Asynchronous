package Promise

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun fetchAsync(url: String, scope: CoroutineScope = GlobalScope) = scope.async {
    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .build()
    val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
    response.await().body()
}

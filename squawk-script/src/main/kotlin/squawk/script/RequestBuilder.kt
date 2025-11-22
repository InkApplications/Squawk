package squawk.script

import kotlinx.serialization.Serializable
import kotlin.collections.plusAssign
import kotlin.io.encoding.Base64

@Serializable
sealed interface RequestBuilder
{
    var name: String?
    var description: String?
    var url: String?
    val headers: MutableList<Pair<String, String>>
}

fun RequestBuilder.header(key: String, value: String) {
    headers += key to value
}

fun RequestBuilder.userAgent(value: String) {
    headers.removeAll { (key, _) -> key == UserAgent }
    header(UserAgent, value)
}

fun RequestBuilder.auth(token: String) {
    header(Auth, token)
}

fun RequestBuilder.basicAuth(username: String, password: String) {
    val token = Base64.encode("$username:$password".encodeToByteArray())
    header(Auth, "Basic $token")
}

fun RequestBuilder.bearerAuth(token: String) {
    header(Auth, "Bearer $token")
}

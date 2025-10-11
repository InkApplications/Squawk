package squawk.script

import kotlin.io.encoding.Base64

private const val UserAgent = "User-Agent"
private const val Auth = "Authorization"

class EndpointBuilder
{
    var method: Method = Method.GET
    var name: String? = null
    var description: String? = null
    var url: String? = null
    var body: String? = null
    val headers: MutableList<Pair<String, String>> = mutableListOf(
        UserAgent to "Squawk/1.0",
    )

    fun header(key: String, value: String)
    {
        headers += key to value
    }

    fun userAgent(value: String)
    {
        headers.removeAll { (key, _) -> key == UserAgent }
        header(UserAgent, value)
    }

    fun auth(token: String) {
        header(Auth, token)
    }

    fun basicAuth(username: String, password: String) {
        val token = Base64.encode("$username:$password".encodeToByteArray())
        header(Auth, "Basic $token")
    }

    fun bearerAuth(token: String) {
        header(Auth, "Bearer $token")
    }
}

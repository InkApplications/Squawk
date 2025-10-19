package squawk.script

import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64

private const val UserAgent = "User-Agent"
private const val Auth = "Authorization"

@Serializable
class EndpointBuilder(
    private val properties: Map<String, String>,
): PropertyContext {
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

    override fun property(key: String, default: String?): String
    {
        return properties[key]
            ?: default
            ?: throw PropertyNotFound(key)
    }

    override fun hasProperty(key: String): Boolean
    {
        return properties.containsKey(key)
    }
}

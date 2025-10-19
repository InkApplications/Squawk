package squawk.script

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Method(val key: String)
{
    companion object
    {
        val GET = Method("GET")
        val POST = Method("POST")
        val PUT = Method("PUT")
        val DELETE = Method("DELETE")
    }
}

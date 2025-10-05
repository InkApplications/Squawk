package squawk.script

@JvmInline
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

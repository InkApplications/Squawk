package squawk.script

interface PropertyContext
{
    fun property(key: String, default: String? = null): String
    fun hasProperty(key: String): Boolean
}


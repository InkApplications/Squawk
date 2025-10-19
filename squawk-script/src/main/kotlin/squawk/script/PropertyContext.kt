package squawk.script

typealias PropertyBag = Map<String, String>

interface PropertyContext
{
    fun property(key: String, default: String? = null): String
    fun hasProperty(key: String): Boolean
}


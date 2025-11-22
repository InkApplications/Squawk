package squawk.script

typealias PropertyBag = Map<String, String>

interface PropertyContext
{
    fun property(key: String, default: String? = null): String
    fun hasProperty(key: String): Boolean
}

fun PropertyBag.toPropertyContext() = object: PropertyContext
{
    override fun property(key: String, default: String?): String {

        return get(key)
            ?: default
            ?: throw PropertyNotFound(key)
    }

    override fun hasProperty(key: String): Boolean {
        return containsKey(key)
    }
}

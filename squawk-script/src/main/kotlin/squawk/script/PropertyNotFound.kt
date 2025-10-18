package squawk.script

import java.io.File

/**
 * A property that was attempted to be retrieved by a script was not found.
 */
class PropertyNotFound(
    val property: String,
    context: File? = null,
): ConfigurationError("Property not found: $property", context) {
    override fun withContext(context: File): ConfigurationError {
        return PropertyNotFound(property, context)
    }
}

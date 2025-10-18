package squawk.script

import java.io.File

/**
 * Required properties file was not found or readable at [path]
 */
class PropertiesFileNotFound(
    val path: String,
    context: File? = null,
): ConfigurationError("Properties file not found: $path", context) {
    override fun withContext(context: File): ConfigurationError {
        return PropertiesFileNotFound(path, context)
    }
}

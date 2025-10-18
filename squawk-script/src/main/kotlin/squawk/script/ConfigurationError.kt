package squawk.script

import java.io.File

/**
 * Script configuration contains an invalid configuration
 */
open class ConfigurationError(
    override val message: String,
    val context: File? = null
): IllegalArgumentException(message) {
    open fun withContext(context: File): ConfigurationError {
        return ConfigurationError(message, context)
    }
}

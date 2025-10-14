package squawk.script

import java.io.File

/**
 * Script configuration contains an invalid configuration
 */
open class ConfigurationError(
    val file: File,
    message: String
): IllegalArgumentException(message)

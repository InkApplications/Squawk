package squawk.script

import java.io.File

/**
 * An included script file was not found at [path]
 */
class IncludedFileNotFound(
    val path: String,
    context: File? = null,
): ConfigurationError("Included file not found: $path", context) {
    override fun withContext(context: File): ConfigurationError {
        return IncludedFileNotFound(path, context)
    }
}

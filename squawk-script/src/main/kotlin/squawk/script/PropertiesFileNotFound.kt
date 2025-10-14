package squawk.script

import java.io.File

/**
 * Required properties file was not found or readable at [path]
 */
class PropertiesFileNotFound(
    file: File,
    path: String
): ConfigurationError(file, "Properties file not found: $path")

package squawk.script

import java.io.File

data class RunConfiguration(
    val target: File,
    val propertyFiles: List<File>,
    val properties: Map<String, String>,
)

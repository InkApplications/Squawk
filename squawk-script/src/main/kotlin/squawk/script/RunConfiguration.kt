package squawk.script

import kotlinx.serialization.Serializable

@Serializable
data class RunConfiguration(
    val target: FileDescriptor,
    val propertyFiles: List<FileDescriptor>,
    val properties: PropertyBag,
) {
    val schemaVersion: Int = 1
}


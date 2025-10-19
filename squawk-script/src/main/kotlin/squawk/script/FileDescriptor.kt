package squawk.script

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class FileDescriptor(
    val absolutePath: String,
    val hash: Sha256Hash,
) {
    val file by lazy { File(absolutePath) }
    suspend fun isValid(): Boolean {
        return withContext(Dispatchers.IO) {
            file.loadDescriptor() == this@FileDescriptor
        }
    }
}

fun File.loadDescriptor(): FileDescriptor
{
    return FileDescriptor(
        absolutePath = absolutePath,
        hash = calculateSha256Hash(),
    )
}

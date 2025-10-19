package squawk.script

import kotlinx.serialization.Serializable
import java.io.File
import kotlin.jvm.java

@JvmInline
@Serializable
value class Sha256Hash(val hex: String)

fun File.calculateSha256Hash(): Sha256Hash
{
    val digest = java.security.MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(readBytes())
    val hexString = hashBytes.toHexString()
    return Sha256Hash(hexString)
}

fun String.calculateSha256Hash(): Sha256Hash
{
    val digest = java.security.MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(toByteArray())
    val hexString = hashBytes.toHexString()
    return Sha256Hash(hexString)
}

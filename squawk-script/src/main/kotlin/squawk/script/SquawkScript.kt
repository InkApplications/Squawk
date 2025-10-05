package squawk.script

import java.io.File
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    fileExtension = "squawk.kts",
    compilationConfiguration = SquawkScriptConfig::class
)
@Suppress("unused")
abstract class SquawkScript(
    val scriptFile: File,
) {
    var endpoints = mutableListOf<EndpointBuilder>()

    fun endpoint(builder: EndpointBuilder.() -> Unit)
    {
        endpoints += EndpointBuilder().apply(builder)
    }
}

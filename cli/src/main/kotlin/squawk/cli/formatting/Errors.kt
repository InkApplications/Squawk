package squawk.cli.formatting

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyle
import squawk.host.ScriptEvaluationException
import squawk.script.ConfigurationError
import java.io.File
import java.nio.channels.UnresolvedAddressException

private val errorLabel = TextStyle(
    bold = true,
    color = TextColors.red,
)

fun printEvaluationError(
    file: File,
    exception: ScriptEvaluationException,
) {
    val locationStyle = TextStyle(
        color = TextColors.yellow,
    )
    println("${errorLabel("Evaluation Error")}: ${exception.message}")
    exception.errors.forEach { error ->
        println()
        val startLine = error.startLine
        val endLine = error.endLine
        val lineCoordinates = when {
            startLine == null -> ""
            endLine == null -> "line ${startLine.line} col ${startLine.column}"
            startLine.line == endLine.line -> "line ${startLine.line} col ${startLine.column}-${endLine.column}"
            else -> "line ${startLine.line} col ${startLine.column} through line ${endLine.line} col ${endLine.column}"
        }
        println(locationStyle("error in '${file.absolutePath}', $lineCoordinates:"))
        println(error.message)
        error.cause?.stackTraceToString()?.run {
            println(this)
        }
    }
}

fun printUnresolvedHost(
    exception: UnresolvedAddressException,
) {
    println("${errorLabel("Network Error")}: Unresolved host address")
}

fun printUnhandledError(
    script: File,
    exception: Throwable,
) {
    println("${errorLabel("Unhandled Error")}: ${exception.message}")
    println("while attempting to run script: '${script.absolutePath}'")
    println(exception.stackTraceToString())
}

fun printConfigurationError(
    exception: ConfigurationError,
) {
    val contextString = exception.context?.name?.let { " in '$it'" }
    println("${errorLabel("Configuration Error")}: ${exception.message}$contextString")
}

fun printBadEndpointArgument(
    value: String,
) {
    println("${errorLabel("Endpoint not found")}: $value")
}

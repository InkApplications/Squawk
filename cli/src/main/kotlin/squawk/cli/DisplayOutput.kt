package squawk.cli

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyle
import squawk.host.ScriptEvaluationException
import squawk.script.EndpointBuilder
import java.io.File
import java.nio.channels.UnresolvedAddressException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object DisplayOutput
{
    private val errorLabel = TextStyle(
        bold = true,
        color = TextColors.red,
    )

    fun title(text: String)
    {
        val style = TextStyle(
            bold = true,
        )
        println(style(text))
    }

    fun endpointLabel(
        label: String,
        endpoint: EndpointBuilder
    ) {
        val keyStyle = TextStyle(
            color = TextColors.green
        )
        val descriptionStyle = TextStyle(
            color = TextColors.yellow,
        )
        val description = endpoint.description ?: endpoint.url
        if (description != null) {
            println("${keyStyle(label)} - ${descriptionStyle(description)}")
        } else {
            println(keyStyle(label))
        }
    }

    fun endpointTitle(name: String)
    {
        val style = TextStyle(
            bold = true,
        )
        println("> " + style("endpoint: $name"))
    }

    fun requestUrl(method: String, url: String)
    {
        val urlStyle = TextStyle(
            color = TextColors.blue,
            underline = true,
            hyperlink = url,
        )
        val methodStyle = TextStyle()

        println("> ${methodStyle(method)}: ${urlStyle(url)}")
    }

    fun rawOutput(text: String)
    {
        println(text)
    }

    fun statusLine(
        code: Int,
        description: String,
        duration: Duration,
    ) {
        val formattedDuration = when {
            duration < 1.seconds -> "${duration.inWholeMilliseconds}ms"
            duration < 1.minutes -> "${duration.inWholeSeconds}s"
            else -> "${duration.inWholeMinutes}m ${(duration.inWholeSeconds % 60)}s"
        }
        val codeStyle = when (code) {
            in 200..299 -> TextStyle(color = TextColors.brightWhite, bgColor = TextColors.green)
            in 300..399 -> TextStyle(color = TextColors.brightWhite, bgColor = TextColors.cyan)
            in 400..499 -> TextStyle(color = TextColors.brightWhite, bgColor = TextColors.red)
            in 500..599 -> TextStyle(color = TextColors.brightWhite, bgColor = TextColors.yellow)
            else -> TextStyle(color = TextColors.brightWhite, bgColor = TextColors.yellow)
        }
        println("> ${codeStyle(" $code ")} $description in $formattedDuration")
    }

    fun scriptEvaluationError(
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

    fun unresolvedHostError(
        exception: UnresolvedAddressException,
    ) {
        println("${errorLabel("Network Error")}: Unresolved host address")
    }

    fun unhandledError(
        script: File,
        exception: Throwable,
    ) {
        println("${errorLabel("Unhandled Error")}: ${exception.message}")
        println("while attempting to run script: '${script.absolutePath}'")
        println(exception.stackTraceToString())
    }
}

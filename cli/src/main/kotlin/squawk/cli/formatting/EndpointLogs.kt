package squawk.cli.formatting

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyle
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun printEndpointTitle(name: String)
{
    printProgress("Request: $name")
}

fun printRequestUrl(method: String, url: String)
{
    val urlStyle = TextStyle(
        color = TextColors.blue,
        underline = true,
        hyperlink = url,
    )
    val methodStyle = TextStyle(
        color = TextColors.green,
    )

    println("${methodStyle(method)}: ${urlStyle(url)}")
}

fun printRequestMeta(key: String, value: String)
{
    val keyStyle = TextStyle(
        color = TextColors.yellow,
    )
    println("${keyStyle(key)}: ${value}")
}

fun printStatus(
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
    println("${codeStyle(" $code ")} $description in $formattedDuration")
}

fun rawOutput(text: String)
{
    println("~~~~~~~~~~")
    println(text)
    println("~~~~~~~~~~")
}

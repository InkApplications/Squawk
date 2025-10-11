package squawk.cli.formatting

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyle
import squawk.script.EndpointBuilder

fun printEndpointLabel(
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
        println("  ${keyStyle(label)} - ${descriptionStyle(description)}")
    } else {
        println("  ${keyStyle(label)}")
    }
}

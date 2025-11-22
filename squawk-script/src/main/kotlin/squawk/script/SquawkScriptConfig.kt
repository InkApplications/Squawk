package squawk.script

import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

object SquawkScriptConfig: ScriptCompilationConfiguration({
    jvm {
        dependenciesFromCurrentContext(wholeClasspath = true)
        defaultImports("squawk.script.Method")
        defaultImports("squawk.script.Method.Companion.GET")
        defaultImports("squawk.script.Method.Companion.POST")
        defaultImports("squawk.script.Method.Companion.PUT")
        defaultImports("squawk.script.Method.Companion.DELETE")
        defaultImports("squawk.script.header")
        defaultImports("squawk.script.userAgent")
        defaultImports("squawk.script.auth")
        defaultImports("squawk.script.basicAuth")
        defaultImports("squawk.script.bearerAuth")
    }
})

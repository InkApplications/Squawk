package squawk.script

import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

object SquawkScriptConfig: ScriptCompilationConfiguration({
    jvm {
        dependenciesFromCurrentContext(wholeClasspath = true)
    }
})

package squawk.script

import java.io.File

interface Evaluator
{
    fun evaluateFile(
        file: File,
        parent: SquawkScript?,
        propertyFiles: List<File>
    ): SquawkScript
}

package squawk.script

interface Evaluator
{
    fun evaluateFile(
        runConfiguration: RunConfiguration,
        parent: SquawkScript?,
    ): SquawkScript
}

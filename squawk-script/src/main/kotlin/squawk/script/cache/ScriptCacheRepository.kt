package squawk.script.cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import squawk.script.RunConfiguration
import squawk.script.ScriptEvaluationResult
import squawk.script.calculateSha256Hash
import java.io.File

class ScriptCacheRepository
{
    private val cacheDir = File(System.getProperty("user.home"), ".squawk/cache")
    private val json = Json { prettyPrint = false; encodeDefaults = true }

    suspend fun getCache(runConfiguration: RunConfiguration): ScriptEvaluationResult?
    {
        return withContext(Dispatchers.IO) {

            val serializedConfig = json.encodeToString(runConfiguration)
            val configHash = serializedConfig.calculateSha256Hash()
            val cacheFile = File(cacheDir, "${configHash.hex}.json")

            cacheFile.takeIf { it.exists() }
                ?.let { json.decodeFromString(it.readText()) }
        }
    }

    suspend fun putCache(
        runConfiguration: RunConfiguration,
        evaluationResult: ScriptEvaluationResult,
    ) {
        return withContext(Dispatchers.IO) {
            val serializedConfig = json.encodeToString(runConfiguration)
            val configHash = serializedConfig.calculateSha256Hash()
            val cacheFile = File(cacheDir, "${configHash.hex}.json")
            cacheDir.mkdirs()

            val cacheEntry = json.encodeToString(evaluationResult)
            cacheFile.writeText(cacheEntry)
        }
    }
}

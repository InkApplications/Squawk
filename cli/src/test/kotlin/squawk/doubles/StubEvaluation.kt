package squawk.doubles

import squawk.script.EndpointBuilder
import squawk.script.FileDescriptor
import squawk.script.RunConfiguration
import squawk.script.ScriptEvaluationResult
import squawk.script.Sha256Hash

val StubEvaluationResult = ScriptEvaluationResult(
    configuration = RunConfiguration(
        target = FileDescriptor("test-file", Sha256Hash("test-hash")),
        propertyFiles = emptyList(),
        properties = emptyMap(),
        parentProperties = emptyMap(),
    ),
    requestBuilders = listOf(),
    children = listOf(),
    namespace = null,
)

val StubEndpoint = EndpointBuilder(emptyMap()).apply {
    url = "https://example.com/"
}

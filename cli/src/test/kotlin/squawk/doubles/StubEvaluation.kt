package squawk.doubles

import squawk.script.EndpointBuilder
import squawk.script.FileDescriptor
import squawk.script.ScriptEvaluationResult
import squawk.script.Sha256Hash

val StubEvaluationResult = ScriptEvaluationResult(
    descriptor = FileDescriptor("test-file", Sha256Hash("test-hash")),
    endpointResults = listOf(),
    children = listOf(),
    namespace = null,
)

val StubEndpoint = EndpointBuilder(emptyMap()).apply {
    url = "https://example.com/"
}

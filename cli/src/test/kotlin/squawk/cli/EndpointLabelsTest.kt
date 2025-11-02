package squawk.cli

import squawk.doubles.StubEndpoint
import squawk.doubles.StubEvaluationResult
import kotlin.test.Test
import kotlin.test.assertEquals

class EndpointLabelsTest
{
    @Test
    fun blankLabel()
    {
        val script = StubEvaluationResult.copy(
            endpointResults = listOf(
                StubEndpoint,
            ),
        )
        val labels = EndpointLabels(script)

        assertEquals(1, labels.names.size, "One label per endpoint")
        assertEquals("get", labels.names[0], "Label defaults to lowercase method name")
    }

    @Test
    fun duplicateDefaults()
    {
        val script = StubEvaluationResult.copy(
            endpointResults = listOf(
                StubEndpoint,
                StubEndpoint.copy(),
            ),
        )
        val labels = EndpointLabels(script)

        assertEquals(2, labels.names.size, "One label per endpoint.")
        assertEquals("get", labels.names[0], "First label uses full default.")
        assertEquals("get-2", labels.names[1], "Second label should be numbered.")
    }

    @Test
    fun named()
    {
        val script = StubEvaluationResult.copy(
            endpointResults = listOf(
                StubEndpoint,
                StubEndpoint.copy(name = "named"),
                StubEndpoint.copy(name = "With formatting"),
            ),
        )
        val labels = EndpointLabels(script)

        assertEquals(3, labels.names.size, "One label per endpoint.")
        assertEquals("get", labels.names[0], "First label uses full default.")
        assertEquals("named", labels.names[1], "Endpoint name is used.")
        assertEquals("with-formatting", labels.names[2], "Endpoint name is lowercased and spaces replaced with dashes.")
    }

    @Test
    fun namespaced()
    {
        val script = StubEvaluationResult.copy(
            namespace = "foo",
            endpointResults = listOf(
                StubEndpoint,
                StubEndpoint.copy(),
                StubEndpoint.copy(name = "named"),
            ),
        )
        val labels = EndpointLabels(script)

        assertEquals(3, labels.names.size, "One label per endpoint.")
        assertEquals("foo:get", labels.names[0], "Namespace is prepended to endpoint with default")
        assertEquals("foo:get-2", labels.names[1], "Namespace is prepended to numbered endpoint.")
        assertEquals("foo:named", labels.names[2], "Namespace is prepended to named endpoint.")
    }

    @Test
    fun children()
    {
        val script = StubEvaluationResult.copy(
            endpointResults = listOf(
                StubEndpoint,
            ),
            children = listOf(
                StubEvaluationResult.copy(
                    namespace = "foo",
                    endpointResults = listOf(
                        StubEndpoint,
                    ),
                ),
            )
        )
        val labels = EndpointLabels(script)

        assertEquals(2, labels.names.size, "One label per endpoint.")
        assertEquals("get", labels.names[0], "Namespace is prepended to endpoint with default")
        assertEquals("foo:get", labels.names[1], "Only child script gets namespace label.")
    }

    @Test
    fun childDuplicates()
    {
        val script = StubEvaluationResult.copy(
            children = listOf(
                StubEvaluationResult.copy(
                    endpointResults = listOf(
                        StubEndpoint.copy(),
                    ),
                ),
                StubEvaluationResult.copy(
                    endpointResults = listOf(
                        StubEndpoint.copy(),
                    ),
                ),
            )
        )
        val labels = EndpointLabels(script)

        assertEquals(2, labels.names.size, "One label per endpoint.")
        assertEquals("get", labels.names[0], "Namespace is prepended to endpoint with default")
        assertEquals("get-2", labels.names[1], "Namespace is prepended to numbered endpoint.")
    }
}

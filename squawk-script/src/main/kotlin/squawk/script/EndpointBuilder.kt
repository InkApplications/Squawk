package squawk.script

import kotlinx.serialization.Serializable

@Serializable
data class EndpointBuilder(
    private val properties: PropertyBag,
    override var name: String? = null,
    override var description: String? = null,
    override var url: String? = null,
    override val headers: MutableList<Pair<String, String>> = mutableListOf(
        UserAgent to "Squawk/1.0",
    ),
    var method: Method = Method.GET,
    var body: String? = null,
    private var dynamics: Boolean = false,
): RequestBuilder, PropertyContext by properties.toPropertyContext() {
    override val hasDynamics: Boolean get() = dynamics

    override fun compute(computation: () -> Unit)
    {
        dynamics = true
        computation()
    }
}

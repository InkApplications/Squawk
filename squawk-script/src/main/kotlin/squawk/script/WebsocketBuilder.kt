package squawk.script

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class WebsocketBuilder(
    private val properties: PropertyBag,
    override var name: String? = null,
    override var description: String? = null,
    override var url: String? = null,
    override val headers: MutableList<Pair<String, String>> = mutableListOf(
        UserAgent to "Squawk/1.0",
    ),
    private var dynamics: Boolean = false,
): RequestBuilder, PropertyContext by properties.toPropertyContext() {
    @Transient
    var onConnect: (suspend OnConnectContext.() -> Unit)? = null
    @Transient
    var onMessage: (suspend OnMessageContext.() -> Unit)? = null
    override val hasDynamics: Boolean get() = dynamics

    fun onConnect(action: suspend OnConnectContext.() -> Unit)
    {
        dynamics = true
        onConnect = action
    }

    fun onMessage(action: suspend OnMessageContext.() -> Unit)
    {
        dynamics = true
        onMessage = action
    }

    override fun compute(computation: () -> Unit)
    {
        dynamics = true
        computation()
    }
}

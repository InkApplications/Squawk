package squawk.script

data class OnMessageContext(
    val message: String,
    private val onSend: suspend (message: String) -> Unit,
) {
    suspend fun send(message: String)
    {
        onSend(message)
    }
}

package squawk.script

data class OnConnectContext(
    private val onSend: suspend (message: String) -> Unit,
) {
    suspend fun send(message: String)
    {
        onSend(message)
    }
}

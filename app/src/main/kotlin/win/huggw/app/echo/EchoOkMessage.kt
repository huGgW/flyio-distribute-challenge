package win.huggw.app.echo

import kotlinx.serialization.Serializable
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.MessageType

const val ECHO_OK_MESSAGE_TYPE = "echo_ok"

@Serializable
data class EchoOkBody(
    val echo: String,
    override val msgId: Int,
    override val inReplyTo: Int,
    override val type: MessageType = ECHO_OK_MESSAGE_TYPE,
): Body {
    init {
        require(type == ECHO_OK_MESSAGE_TYPE)
    }
}
package win.huggw.app.echo

import kotlinx.serialization.Serializable
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.MessageType

const val ECHO_MESSAGE_TYPE = "echo"
const val ECHO_OK_MESSAGE_TYPE = "echo_ok"

@Serializable
data class EchoBody(
    val echo: String,
    override val msgId: Int,
    override val type: MessageType = ECHO_MESSAGE_TYPE,
): Body {
    override val inReplyTo = null

    init {
        require(type == ECHO_MESSAGE_TYPE)
    }

    fun reply(msgId: Int) = EchoOkBody(
        echo = echo,
        msgId = msgId,
        inReplyTo = this.msgId,
    )
}

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

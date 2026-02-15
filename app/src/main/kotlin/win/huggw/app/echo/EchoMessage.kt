package win.huggw.app.echo

import kotlinx.serialization.Serializable
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.MessageType

const val ECHO_MESSAGE_TYPE = "echo"

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
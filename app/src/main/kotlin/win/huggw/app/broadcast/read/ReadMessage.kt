package win.huggw.app.broadcast.read

import kotlinx.serialization.Serializable
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.MessageType

const val READ_MESSAGE_TYPE = "read"
const val READ_OK_MESSAGE_TYPE = "read_ok"

@Serializable
data class ReadBody(
    override val msgId: Int,
    override val type: MessageType = READ_MESSAGE_TYPE,
) : Body {
    override val inReplyTo = null

    init {
        require(type == READ_MESSAGE_TYPE)
    }

    fun reply(
        msgId: Int,
        messages: List<Int>,
    ) = ReadOkBody(messages, msgId, this.msgId)
}

@Serializable
data class ReadOkBody(
    val messages: List<Int>,
    override val msgId: Int,
    override val inReplyTo: Int,
    override val type: MessageType = READ_OK_MESSAGE_TYPE,
) : Body {
    init {
        require(type == READ_OK_MESSAGE_TYPE)
    }
}

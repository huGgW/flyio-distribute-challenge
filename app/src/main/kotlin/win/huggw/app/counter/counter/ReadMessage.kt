package win.huggw.app.counter.counter

import kotlinx.serialization.Serializable
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.MessageType

const val READ_MESSAGE_TYPE = "read"
const val READ_OK_MESSAGE_TYPE = "read_ok"

@Serializable
data class ReadBody(
    override val msgId: Int,
    override val type: MessageType = WRITE_MESSAGE_TYPE,
): Body {
    override val inReplyTo = null

    init {
        require(type == WRITE_MESSAGE_TYPE)
    }

    fun reply(value: Int, msgId: Int) = ReadOkBody(
        value,
        msgId,
        inReplyTo = this.msgId,
    )
}

@Serializable
data class ReadOkBody(
    val value: Int,
    override val msgId: Int,
    override val inReplyTo: Int,
    override val type: MessageType = READ_OK_MESSAGE_TYPE,
): Body {
    init {
        require(type == READ_OK_MESSAGE_TYPE)
    }
}

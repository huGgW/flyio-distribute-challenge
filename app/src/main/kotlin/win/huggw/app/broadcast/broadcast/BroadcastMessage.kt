package win.huggw.app.broadcast.broadcast

import kotlinx.serialization.Serializable
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.MessageType

const val BROADCAST_MESSAGE_TYPE = "broadcast"
const val BROADCAST_OK_MESSAGE_TYPE = "broadcast_ok"

@Serializable
data class BroadcastBody(
    val message: Int,
    override val type: MessageType = BROADCAST_MESSAGE_TYPE,
    override val msgId: Int,
) : Body {
    override val inReplyTo = null

    init {
        require(type == BROADCAST_MESSAGE_TYPE)
    }

    fun reply(msgId: Int) = BroadcastOkBody(msgId, this.msgId)
}

@Serializable
data class BroadcastOkBody(
    override val msgId: Int,
    override val inReplyTo: Int,
    override val type: MessageType = BROADCAST_OK_MESSAGE_TYPE,
) : Body {
    init {
        require(type == BROADCAST_OK_MESSAGE_TYPE)
    }
}

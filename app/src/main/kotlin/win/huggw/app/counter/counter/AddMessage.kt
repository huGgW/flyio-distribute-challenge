package win.huggw.app.counter.counter

import kotlinx.serialization.Serializable
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.MessageType

const val ADD_MESSAGE_TYPE = "add"
const val ADD_OK_MESSAGE_TYPE = "add_ok"

@Serializable
data class AddBody(
    val delta: Int,
    override val msgId: Int,
    override val type: MessageType = ADD_MESSAGE_TYPE,
) : Body {
    override val inReplyTo = null

    init {
        require(type == ADD_MESSAGE_TYPE)
    }

    fun reply(msgId: Int) =
        AddOkBody(
            msgId = msgId,
            inReplyTo = this.msgId,
        )
}

@Serializable
data class AddOkBody(
    override val msgId: Int,
    override val inReplyTo: Int,
    override val type: MessageType = ADD_OK_MESSAGE_TYPE,
) : Body {
    init {
        require(type == ADD_OK_MESSAGE_TYPE)
    }
}

package win.huggw.app.counter.seqkv.message

import kotlinx.serialization.Serializable
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.MessageType

const val WRITE_MESSAGE_TYPE = "kv_write"
const val WRITE_OK_MESSAGE_TYPE = "kv_write_ok"

@Serializable
data class WriteBody(
    val key: String,
    val value: Int,
    override val msgId: Int,
    override val type: MessageType = WRITE_MESSAGE_TYPE,
): Body {
    override val inReplyTo = null

    init {
        require(type == WRITE_MESSAGE_TYPE)
    }

    fun reply(msgId: Int) = WriteOkBody(
        value,
        msgId,
    )
}

@Serializable
data class WriteOkBody(
    override val msgId: Int,
    override val inReplyTo: Int,
    override val type: MessageType = WRITE_OK_MESSAGE_TYPE,
): Body {
    init {
        require(type == WRITE_OK_MESSAGE_TYPE)
    }
}

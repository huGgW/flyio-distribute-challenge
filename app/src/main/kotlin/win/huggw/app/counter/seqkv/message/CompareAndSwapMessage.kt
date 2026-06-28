package win.huggw.app.counter.seqkv.message

import kotlinx.serialization.Serializable
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.MessageType

const val CAS_MESSAGE_TYPE = "cas"
const val CAS_OK_MESSAGE_TYPE = "cas_ok"

@Serializable
data class CompareAndSwapBody(
    val key: String,
    val from: Int,
    val to: Int,
    override val msgId: Int,
    override val type: MessageType = CAS_MESSAGE_TYPE
): Body {
    override val inReplyTo = null

    init {
        require(type == CAS_MESSAGE_TYPE)
    }

    fun reply(msgId: Int) = CompareAndSwapOkBody(
        msgId,
        inReplyTo = this.msgId,
    )
}

@Serializable
data class CompareAndSwapOkBody(
    override val msgId: Int,
    override val inReplyTo: Int,
    override val type: MessageType = CAS_OK_MESSAGE_TYPE
): Body {
    init {
        require(type == CAS_OK_MESSAGE_TYPE)
    }
}

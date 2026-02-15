package win.huggw.maelstrom.init

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.MessageType

const val INIT_OK_MESSAGE_TYPE: MessageType = "init_ok"

@Serializable
data class InitOkBody (
    override val msgId: Int,
    override val inReplyTo: Int,
    override val type: MessageType = INIT_OK_MESSAGE_TYPE,
): Body {
    init {
        require(type == INIT_OK_MESSAGE_TYPE)
    }
}
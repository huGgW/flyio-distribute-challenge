package win.huggw.maelstrom.init

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.MessageType

const val INIT_MESSAGE_TYPE: MessageType = "init"

@Serializable
data class InitBody(
    override val msgId: Int,
    val nodeId: String,
    val nodeIds: List<String>,
    override val type: MessageType = INIT_MESSAGE_TYPE,
) : Body {
    override val inReplyTo = null

    init {
        require(type == INIT_MESSAGE_TYPE)
    }
}
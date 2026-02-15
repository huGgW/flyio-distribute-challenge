package win.huggw.maelstrom.init

import kotlinx.serialization.Serializable
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.Message
import win.huggw.maelstrom.message.MessageType

val INIT_MESSAGE_TYPE: MessageType = "init"

typealias InitMessage = Message<InitBody>

@Serializable
data class InitBody(
    override val msgId: Int,
    val nodeId: String,
    val nodeIds: List<String>,
): Body {
    override val type = INIT_MESSAGE_TYPE
    override val inReplyTo = null
}
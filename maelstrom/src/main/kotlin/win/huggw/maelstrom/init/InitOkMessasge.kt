package win.huggw.maelstrom.init

import kotlinx.serialization.Serializable
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.Message
import win.huggw.maelstrom.message.MessageType

val INIT_OK_MESSAGE_TYPE: MessageType = "init_ok"

typealias InitOkMessage = Message<InitOkBody>

@Serializable
data class InitOkBody (
    override val inReplyTo: Int,
): Body {
    override val type = INIT_OK_MESSAGE_TYPE
    override val msgId = null
}
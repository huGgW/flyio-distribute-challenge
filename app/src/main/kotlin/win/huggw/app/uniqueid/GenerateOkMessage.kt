package win.huggw.app.uniqueid

import kotlinx.serialization.Serializable
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.MessageType

const val GENERATE_OK_MESSAGE_TYPE = "generate_ok"

@Serializable
data class GenerateOkBody(
    val id: String,
    override val type: MessageType = GENERATE_OK_MESSAGE_TYPE,
    override val msgId: Int,
    override val inReplyTo: Int,
): Body {
    init {
        require(type == GENERATE_OK_MESSAGE_TYPE)
    }

}
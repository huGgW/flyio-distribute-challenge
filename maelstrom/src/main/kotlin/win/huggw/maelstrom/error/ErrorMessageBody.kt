package win.huggw.maelstrom.error

import kotlinx.serialization.Serializable
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.MessageType

const val ERROR_MESSAGE_TYPE = "error"

@Serializable
data class ErrorMessageBody(
    val name: String,
    val code: Int,
    val text: String? = null,
    override val type: MessageType = ERROR_MESSAGE_TYPE,
    override val inReplyTo: Int? = null,
) : Body {
    override val msgId = null

    init {
        require(type == ERROR_MESSAGE_TYPE)
    }
}

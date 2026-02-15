package win.huggw.maelstrom.error

import kotlinx.serialization.Serializable
import win.huggw.maelstrom.message.Body

@Serializable
data class ErrorMessageBody(
    val name: String,
    val code: Int,
    val text: String? = null,
    override val inReplyTo: Int? = null,
) : Body {
    override val type = "error"
    override val msgId = null
}

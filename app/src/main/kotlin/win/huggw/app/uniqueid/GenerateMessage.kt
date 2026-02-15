package win.huggw.app.uniqueid

import kotlinx.serialization.Serializable
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.MessageType

const val GENERATE_MESSAGE_TYPE = "generate"

@Serializable
data class GenerateBody(
    override val msgId: Int,
    override val type: MessageType = GENERATE_MESSAGE_TYPE,
): Body {
    override val inReplyTo = null
    init {
        require(type == GENERATE_MESSAGE_TYPE)
    }

    fun reply(uniqueId: String, msgId: Int) = GenerateOkBody(
        id = uniqueId,
        msgId = msgId,
        inReplyTo = this.msgId,
        type = GENERATE_OK_MESSAGE_TYPE,
    )

}
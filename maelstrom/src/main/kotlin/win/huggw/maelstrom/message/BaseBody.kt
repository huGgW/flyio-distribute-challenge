package win.huggw.maelstrom.message

import kotlinx.serialization.Serializable

@Serializable
data class BaseBody(
    override val type: MessageType,
    override val msgId: Int,
    override val inReplyTo: Int?,
) : Body

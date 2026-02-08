package win.huggw.maelstrom_node

import kotlinx.serialization.Serializable

typealias MessageType = String

@Serializable
open class Message (
    val type: MessageType,
    val msgId: String?,
    val inReplyTo: String?,
)

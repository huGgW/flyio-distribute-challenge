package win.huggw.maelstrom_server

import kotlinx.serialization.Serializable

typealias MessageType = String

@Serializable
open class Message (
    val type: MessageType,
    val msgId: String?,
    val inReplyTo: String?,
)

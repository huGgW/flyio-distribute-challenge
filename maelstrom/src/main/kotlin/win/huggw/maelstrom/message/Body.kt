package win.huggw.maelstrom.message

typealias MessageType = String

interface Body {
    val type: MessageType
    val msgId: Int
    val inReplyTo: Int?
}

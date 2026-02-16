package win.huggw.maelstrom.handler

import win.huggw.maelstrom.error.MaelstromError
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.Message
import win.huggw.maelstrom.message.MessageType
import win.huggw.maelstrom.node.NodeContext

interface Handler<B : Body> {
    val messageType: MessageType

    @Throws(MaelstromError::class)
    suspend fun handle(
        ctx: NodeContext,
        message: Message<B>,
    )
}

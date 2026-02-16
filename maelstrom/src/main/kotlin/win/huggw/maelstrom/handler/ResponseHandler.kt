package win.huggw.maelstrom.handler

import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.Message
import win.huggw.maelstrom.message.MessageType
import win.huggw.maelstrom.node.NodeContext
import win.huggw.maelstrom.node.ResponseNodeContext

class ResponseHandler<B : Body>(
    override val messageType: MessageType,
) : Handler<B> {
    override suspend fun handle(
        ctx: NodeContext,
        message: Message<B>,
    ) {
        require(ctx is ResponseNodeContext)

        if (message.body.inReplyTo != null) {
            ctx.receiveReply(message)
        }
    }
}

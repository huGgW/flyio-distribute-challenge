package win.huggw.maelstrom.init

import win.huggw.maelstrom.handler.Handler
import win.huggw.maelstrom.message.Message
import win.huggw.maelstrom.node.InitNodeContext
import win.huggw.maelstrom.node.NodeContext
import win.huggw.maelstrom.node.send

class InitHandler : Handler<InitBody> {
    override val messageType = INIT_MESSAGE_TYPE

    override suspend fun handle(
        ctx: NodeContext,
        message: Message<InitBody>,
    ) {
        require(ctx is InitNodeContext)

        ctx.setId(message.body.nodeId)
        ctx.setNodeIds(message.body.nodeIds.toSet())

        ctx.send(
            message.replyTo(
                InitOkBody(
                    msgId = ctx.nextMessageId(),
                    message.body.msgId,
                ),
            ),
        )
    }
}

package win.huggw.maelstrom.init

import win.huggw.maelstrom.handler.Handler
import win.huggw.maelstrom.message.Message
import win.huggw.maelstrom.node.InternalNodeContext
import win.huggw.maelstrom.node.NodeContext
import win.huggw.maelstrom.node.push

class InitHandler (
): Handler<InitBody> {
    override val messageType = INIT_MESSAGE_TYPE

    override suspend fun handle(ctx: NodeContext, message: Message<InitBody>) {
        require(ctx is InternalNodeContext)

        ctx.setId(message.body.nodeId)

        // Note: record node ids if needed

        ctx.push(
            message.replyTo(
                InitOkBody(
                    message.body.msgId,
                ),
            ),
        )
    }
}
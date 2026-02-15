package win.huggw.app.echo

import win.huggw.maelstrom.handler.Handler
import win.huggw.maelstrom.message.Message
import win.huggw.maelstrom.message.MessageType
import win.huggw.maelstrom.node.NodeContext
import win.huggw.maelstrom.node.push

class EchoHandler: Handler<EchoBody> {
    override val messageType: MessageType = ECHO_MESSAGE_TYPE

    override suspend fun handle(
        ctx: NodeContext,
        message: Message<EchoBody>
    ) {
        ctx.log("echoing '${message.body.echo}'")

        ctx.push(
            message.replyTo(
                message.body.reply(
                    ctx.nextMessageId(),
                ),
            ),
        )
    }
}
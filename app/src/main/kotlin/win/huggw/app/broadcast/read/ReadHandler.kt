package win.huggw.app.broadcast.read

import win.huggw.app.broadcast.Repository
import win.huggw.maelstrom.handler.Handler
import win.huggw.maelstrom.message.Message
import win.huggw.maelstrom.message.MessageType
import win.huggw.maelstrom.node.NodeContext
import win.huggw.maelstrom.node.send

class ReadHandler(
    private val repository: Repository,
) : Handler<ReadBody> {
    override val messageType: MessageType = READ_MESSAGE_TYPE

    override suspend fun handle(
        ctx: NodeContext,
        message: Message<ReadBody>,
    ) {
        val messages = repository.getMessages()

        ctx.send(
            message.replyTo(
                message.body.reply(
                    msgId = ctx.nextMessageId(),
                    messages = messages.toList(),
                ),
            ),
        )
    }
}

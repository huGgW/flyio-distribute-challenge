package win.huggw.app.uniqueid

import win.huggw.maelstrom.handler.Handler
import win.huggw.maelstrom.message.Message
import win.huggw.maelstrom.message.MessageType
import win.huggw.maelstrom.node.NodeContext
import win.huggw.maelstrom.node.send
import kotlin.random.Random
import kotlin.time.Clock

class UniqueIdHandler : Handler<GenerateBody> {
    override val messageType: MessageType = GENERATE_MESSAGE_TYPE

    override suspend fun handle(
        ctx: NodeContext,
        message: Message<GenerateBody>,
    ) {
        // val nodeId = ctx.id
        //
        // // this part will ensure generate id will defer from past node which has same node id
        // val currentMillis = Clock.System.now().toEpochMilliseconds()
        //
        // // this part add randomness for just in case
        // val randHex = Random.nextInt().toString(16)
        //

        val replyMsgId = ctx.nextMessageId()
        val uniqueId = generate(ctx.id, replyMsgId)

        ctx.send(
            message.replyTo(
                message.body.reply(uniqueId, replyMsgId),
            ),
        )
    }
}

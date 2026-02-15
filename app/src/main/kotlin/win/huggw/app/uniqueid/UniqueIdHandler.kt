package win.huggw.app.uniqueid

import win.huggw.maelstrom.handler.Handler
import win.huggw.maelstrom.message.Message
import win.huggw.maelstrom.message.MessageType
import win.huggw.maelstrom.node.NodeContext
import win.huggw.maelstrom.node.push
import kotlin.random.Random
import kotlin.time.Clock

class UniqueIdHandler: Handler<GenerateBody> {
    override val messageType: MessageType = GENERATE_MESSAGE_TYPE

    override suspend fun handle(
        ctx: NodeContext,
        message: Message<GenerateBody>
    ) {
        // this part will ensure generate id is unique in current nodes
        val nodeId = ctx.id
        val replyMsgId = ctx.nextMessageId()

        // this part will ensure generate id will defer from past node which has same node id
        val currentMillis = Clock.System.now().toEpochMilliseconds()

        // this part add randomness for just in case
        val randHex = Random.nextInt().toString(16)

        val uniqueId = "$currentMillis-$nodeId-$replyMsgId-$randHex"

        ctx.log("Generate unique id: $uniqueId")

        ctx.push(
            message.replyTo(
                message.body.reply(uniqueId, replyMsgId)
            )
        )
    }
}
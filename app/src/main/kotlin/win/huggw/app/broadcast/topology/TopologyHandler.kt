package win.huggw.app.broadcast.topology

import win.huggw.app.broadcast.Repository
import win.huggw.maelstrom.handler.Handler
import win.huggw.maelstrom.message.Message
import win.huggw.maelstrom.node.NodeContext
import win.huggw.maelstrom.node.push

class TopologyHandler(
    private val repository: Repository,
) : Handler<TopologyBody> {
    override val messageType: String = TOPOLOGY_MESSAGE_TYPE

    override suspend fun handle(
        ctx: NodeContext,
        message: Message<TopologyBody>,
    ) {
        val topology = message.body.topology

        repository.updateTopology(topology)

        ctx.push(
            message.replyTo(
                message.body.reply(ctx.nextMessageId()),
            ),
        )
    }
}

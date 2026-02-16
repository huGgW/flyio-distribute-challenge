package win.huggw.app.broadcast.broadcast

import win.huggw.app.broadcast.Repository
import win.huggw.maelstrom.handler.Handler
import win.huggw.maelstrom.message.Message
import win.huggw.maelstrom.message.MessageType
import win.huggw.maelstrom.node.NodeContext
import win.huggw.maelstrom.node.push

class BroadcastHandler(
    private val repository: Repository,
) : Handler<BroadcastBody> {
    override val messageType: MessageType = BROADCAST_MESSAGE_TYPE

    override suspend fun handle(
        ctx: NodeContext,
        message: Message<BroadcastBody>,
    ) {
        val broadcastedMessage = message.body.message
        if (!repository.addMessage(broadcastedMessage)) {
            respondOk(ctx, message)
            return
        }

        val sendNodeIds =
            calculatePropagateSubjects(
                nodeIds = ctx.nodeIds,
                topology = repository.getTopology(),
                messageSrc = message.src,
                currentNode = ctx.id,
            )

        sendNodeIds.forEach { propagate(ctx, it, message) }

        respondOk(ctx, message)
    }

    private fun calculatePropagateSubjects(
        nodeIds: Set<String>,
        topology: Map<String, Set<String>>,
        messageSrc: String,
        currentNode: String,
    ): Set<String> {
        val coveredNodeIds = mutableSetOf<String>(currentNode)
        val sendNodeIds = mutableSetOf<String>()

        // if message is from a node, neighbors of that node is already covered
        if (messageSrc in nodeIds) {
            coveredNodeIds.add(messageSrc)
            traverseAndCover(topology, messageSrc, coveredNodeIds)
        }

        // send neighbor node first, and message should be covered by topology
        if (currentNode in topology && topology[currentNode]!!.isNotEmpty()) {
            val toSendNeighbors = topology[currentNode]!! - coveredNodeIds

            coveredNodeIds.addAll(toSendNeighbors)
            sendNodeIds.addAll(toSendNeighbors)

            for (sendNodeId in toSendNeighbors) {
                traverseAndCover(topology, sendNodeId, coveredNodeIds)
            }
        }

        // directly send message to nodes that cannot be covered by topology
        sendNodeIds.addAll(nodeIds - coveredNodeIds)

        return sendNodeIds
    }

    private fun traverseAndCover(
        topology: Map<String, Set<String>>,
        from: String,
        covered: MutableSet<String>,
    ) {
        if (from !in topology || topology[from]!!.isNotEmpty()) {
            return
        }

        val neighbors = topology[from]!!
        val toTraverses = mutableListOf<String>()

        for (neighbor in neighbors) {
            if (neighbor in covered) {
                continue
            }

            covered.add(neighbor)
            toTraverses.add(neighbor)
        }

        for (toTraverse in toTraverses) {
            traverseAndCover(topology, toTraverse, covered)
        }
    }

    private suspend fun propagate(
        ctx: NodeContext,
        nodeId: String,
        message: Message<BroadcastBody>,
    ) {
        ctx.push(
            message.copy(
                src = ctx.id,
                dest = nodeId,
                body = message.body.copy(msgId = ctx.nextMessageId()),
            ),
        )
    }

    private suspend fun respondOk(
        ctx: NodeContext,
        message: Message<BroadcastBody>,
    ) {
        ctx.push(
            message.replyTo(
                message.body.reply(ctx.nextMessageId()),
            ),
        )
    }
}

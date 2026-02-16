package win.huggw.app.broadcast.topology

import kotlinx.serialization.Serializable
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.MessageType

const val TOPOLOGY_MESSAGE_TYPE = "topology"
const val TOPOLOGY_OK_MESSAGE_TYPE = "topology_ok"

@Serializable
data class TopologyBody(
    val topology: Map<String, Set<String>>,
    override val msgId: Int,
    override val type: MessageType = TOPOLOGY_MESSAGE_TYPE,
) : Body {
    override val inReplyTo = null

    init {
        require(type == TOPOLOGY_MESSAGE_TYPE)
    }

    fun reply(msgId: Int) = TopologyOkBody(msgId, this.msgId)
}

@Serializable
data class TopologyOkBody(
    override val msgId: Int,
    override val inReplyTo: Int,
    override val type: MessageType = TOPOLOGY_OK_MESSAGE_TYPE,
) : Body {
    init {
        require(type == TOPOLOGY_OK_MESSAGE_TYPE)
    }
}

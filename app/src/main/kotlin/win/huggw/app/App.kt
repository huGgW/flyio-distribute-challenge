package win.huggw.app

import win.huggw.app.broadcast.Repository
import win.huggw.app.broadcast.broadcast.BROADCAST_OK_MESSAGE_TYPE
import win.huggw.app.broadcast.broadcast.BroadcastHandler
import win.huggw.app.broadcast.broadcast.BroadcastOkBody
import win.huggw.app.broadcast.read.READ_OK_MESSAGE_TYPE
import win.huggw.app.broadcast.read.ReadHandler
import win.huggw.app.broadcast.read.ReadOkBody
import win.huggw.app.broadcast.topology.TOPOLOGY_OK_MESSAGE_TYPE
import win.huggw.app.broadcast.topology.TopologyHandler
import win.huggw.app.broadcast.topology.TopologyOkBody
import win.huggw.app.echo.ECHO_OK_MESSAGE_TYPE
import win.huggw.app.echo.EchoHandler
import win.huggw.app.echo.EchoOkBody
import win.huggw.app.uniqueid.GENERATE_OK_MESSAGE_TYPE
import win.huggw.app.uniqueid.GenerateOkBody
import win.huggw.app.uniqueid.UniqueIdHandler
import win.huggw.maelstrom.node.Node

suspend fun main() {
    val repository = Repository()

    val node =
        Node {
            addHandler(EchoHandler())
            addResponse<EchoOkBody>(ECHO_OK_MESSAGE_TYPE)

            addHandler(UniqueIdHandler())
            addResponse<GenerateOkBody>(GENERATE_OK_MESSAGE_TYPE)

            addHandler(BroadcastHandler(repository))
            addResponse<BroadcastOkBody>(BROADCAST_OK_MESSAGE_TYPE)

            addHandler(TopologyHandler(repository))
            addResponse<TopologyOkBody>(TOPOLOGY_OK_MESSAGE_TYPE)

            addHandler(ReadHandler(repository))
            addResponse<ReadOkBody>(READ_OK_MESSAGE_TYPE)
        }

    node.listen()
}

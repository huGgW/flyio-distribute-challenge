package win.huggw.app

import win.huggw.app.broadcast.Repository
import win.huggw.app.broadcast.broadcast.BroadcastHandler
import win.huggw.app.broadcast.read.ReadHandler
import win.huggw.app.broadcast.topology.TopologyHandler
import win.huggw.app.echo.EchoHandler
import win.huggw.app.uniqueid.UniqueIdHandler
import win.huggw.maelstrom.node.Node

suspend fun main() {
    val repository = Repository()

    val node =
        Node {
            addHandler(EchoHandler())

            addHandler(UniqueIdHandler())

            addHandler(BroadcastHandler(repository))
            addHandler(TopologyHandler(repository))
            addHandler(ReadHandler(repository))
        }

    node.listen()
}

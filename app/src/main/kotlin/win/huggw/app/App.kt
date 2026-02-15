package win.huggw.app

import win.huggw.app.echo.EchoHandler
import win.huggw.app.uniqueid.UniqueIdHandler
import win.huggw.maelstrom.node.Node

suspend fun main() {
    val node = Node {
        addHandler(EchoHandler())
        addHandler(UniqueIdHandler())
    }

    node.listen()
}

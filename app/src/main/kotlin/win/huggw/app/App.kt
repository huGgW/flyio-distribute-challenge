package win.huggw.app

import win.huggw.app.echo.EchoHandler
import win.huggw.maelstrom.node.Node

suspend fun main() {
    val node = Node {
        addHandler(EchoHandler())
    }

    node.listen()
}

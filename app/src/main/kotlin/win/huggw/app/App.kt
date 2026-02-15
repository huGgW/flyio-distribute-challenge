package win.huggw.app

import win.huggw.maelstrom.node.Node

suspend fun main() {
    val node = Node {}

    node.listen()
}

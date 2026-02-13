package win.huggw.app

import win.huggw.maelstrom.Node

suspend fun main() {
    val node = Node {}

    node.listen()
}

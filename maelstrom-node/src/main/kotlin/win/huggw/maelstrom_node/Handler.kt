package win.huggw.maelstrom_node

interface Handler {
    fun handle(message: Message)
}
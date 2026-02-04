package win.huggw.maelstrom_server

interface Handler {
    fun handle(message: Message)
}
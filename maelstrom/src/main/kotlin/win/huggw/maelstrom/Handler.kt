package win.huggw.maelstrom

interface Handler {
    @Throws(Exception::class)
    fun handle(message: Message)
}

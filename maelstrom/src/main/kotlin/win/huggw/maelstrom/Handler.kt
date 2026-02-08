package win.huggw.maelstrom

interface Handler {
    @Throws(Error::class)
    fun handle(message: Message)
}

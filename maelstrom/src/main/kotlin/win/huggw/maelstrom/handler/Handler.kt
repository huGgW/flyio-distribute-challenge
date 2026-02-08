package win.huggw.maelstrom.handler

import win.huggw.maelstrom.error.MaelstromError
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.Message
import win.huggw.maelstrom.message.MessageType

interface Handler<B : Body> {
    val messageType: MessageType

    @Throws(MaelstromError::class)
    fun handle(message: Message<B>)
}

package win.huggw.maelstrom.node

import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.Message
import kotlin.reflect.KClass

interface NodeContext {
    val id: String

    suspend fun <B: Body> push(message: Message<B>, bodyClass: KClass<B>)

    suspend fun log(message: String)
}

suspend inline fun <reified B: Body> NodeContext.push(message: Message<B>) {
    push(message, B::class)
}

internal interface InternalNodeContext: NodeContext {
    fun setId(id: String)
}
package win.huggw.maelstrom.node

import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.Message
import kotlin.reflect.KClass

interface NodeContext {
    val id: String
    val nodeIds: Set<String>

    fun nextMessageId(): Int

    suspend fun <B : Body> push(
        message: Message<B>,
        bodyClass: KClass<B>,
    )

    suspend fun log(message: String)
}

suspend inline fun <reified B : Body> NodeContext.push(message: Message<B>) {
    push(message, B::class)
}

internal interface InitNodeContext : NodeContext {
    fun setId(id: String)

    fun setNodeIds(nodeIds: Set<String>)
}

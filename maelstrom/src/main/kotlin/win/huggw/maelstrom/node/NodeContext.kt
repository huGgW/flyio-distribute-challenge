package win.huggw.maelstrom.node

import kotlinx.coroutines.CompletableDeferred
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.Message
import kotlin.reflect.KClass

interface NodeContext {
    val id: String
    val nodeIds: Set<String>

    fun nextMessageId(): Int

    suspend fun <B : Body> send(
        message: Message<B>,
        bodyClass: KClass<B>,
    )

    suspend fun <B : Body> rpc(
        message: Message<B>,
        bodyClass: KClass<B>,
    ): CompletableDeferred<Message<out Body>>

    suspend fun log(message: String)
}

internal interface InitNodeContext : NodeContext {
    fun setId(id: String)

    fun setNodeIds(nodeIds: Set<String>)
}

interface ResponseNodeContext : NodeContext {
    suspend fun receiveReply(message: Message<out Body>)
}

suspend inline fun <reified B : Body> NodeContext.send(message: Message<B>) {
    send(message, B::class)
}

suspend inline fun <reified B : Body> NodeContext.rpc(message: Message<B>) = rpc(message, B::class)

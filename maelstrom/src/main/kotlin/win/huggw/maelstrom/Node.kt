package win.huggw.maelstrom

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

class Node internal constructor(
    val handler: Map<MessageType, Handler>,
    val json: Json,
) {
    fun listen() {
        TODO()
    }
}

class NodeBuilder internal constructor() {
    private val handlers = mutableMapOf<MessageType, Handler>()

    @OptIn(ExperimentalSerializationApi::class)
    var json: Json =
        Json {
            namingStrategy = JsonNamingStrategy.SnakeCase
            ignoreUnknownKeys = true
        }

    fun handle(adderAction: HandlerInfoAdder.() -> Unit) {
        val adder = HandlerInfoAdder()
        adder.adderAction()
        adder.add(handlers)
    }

    class HandlerInfoAdder internal constructor() {
        lateinit var messageType: MessageType
        lateinit var handler: Handler

        internal fun add(handlers: MutableMap<MessageType, Handler>) {
            require(messageType !in handlers)
            handlers[messageType] = handler
        }
    }

    fun build() = Node(handlers, json)
}

fun Node(builderAction: NodeBuilder.() -> Unit): Node {
    val builder = NodeBuilder()
    builder.builderAction()
    return builder.build()
}

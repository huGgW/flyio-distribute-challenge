package win.huggw.maelstrom.node

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import win.huggw.maelstrom.error.ERROR_MESSAGE_TYPE
import win.huggw.maelstrom.error.ErrorMessageBody
import win.huggw.maelstrom.handler.GeneralHandler
import win.huggw.maelstrom.handler.Handler
import win.huggw.maelstrom.handler.ResponseHandler
import win.huggw.maelstrom.init.InitHandler
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.MessageType
import kotlin.reflect.KClass

class NodeBuilder internal constructor() {
    internal val handlers = mutableMapOf<MessageType, GeneralHandler>()

    init {
        // add init handler
        addHandler(InitHandler())

        // add error response handler
        addResponse<ErrorMessageBody>(ERROR_MESSAGE_TYPE)
    }

    @OptIn(ExperimentalSerializationApi::class)
    var json: Json =
        Json {
            namingStrategy = JsonNamingStrategy.SnakeCase
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    inline fun <reified B : Body> addHandler(handler: Handler<B>) {
        addHandler(handler, B::class)
    }

    fun addHandler(
        handler: Handler<out Body>,
        bodyClass: KClass<out Body>,
    ) {
        val adapter = GeneralHandler(handler, bodyClass)
        require(adapter.messageType !in handlers)
        handlers[adapter.messageType] = adapter
    }

    inline fun <reified R : Body> addResponse(okMessageType: MessageType) {
        addHandler(ResponseHandler(okMessageType), R::class)
    }

    fun build() = Node(handlers, json)
}

fun Node(builderAction: NodeBuilder.() -> Unit): Node {
    val builder = NodeBuilder()
    builder.builderAction()
    return builder.build()
}

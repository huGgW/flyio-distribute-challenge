package win.huggw.maelstrom_server

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

class Server internal constructor (
    val handler: Map<MessageType, Handler>,
    val json: Json,
) {
    fun listen() {
        TODO()
    }
}

class ServerBuilder internal constructor () {
    private val handlers = mutableMapOf<MessageType, Handler>()

    @OptIn(ExperimentalSerializationApi::class)
    var json: Json = Json {
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

    fun build() = Server(handlers, json)
}

fun Server(builderAction: ServerBuilder.() -> Unit): Server {
    val builder = ServerBuilder()
    builder.builderAction()
    return builder.build()
}
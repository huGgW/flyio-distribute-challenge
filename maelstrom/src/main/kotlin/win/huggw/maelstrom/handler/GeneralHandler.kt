package win.huggw.maelstrom.handler

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerOrNull
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.Message
import win.huggw.maelstrom.message.RawMessage
import win.huggw.maelstrom.node.InternalNodeContext
import win.huggw.maelstrom.node.Node
import kotlin.reflect.KClass
import kotlin.reflect.cast

@OptIn(InternalSerializationApi::class)
internal class GeneralHandler(
    val handler: Handler<out Body>,
    val bodyClass: KClass<out Body>,
) {
    val messageType = handler.messageType
    val deserializer =
        bodyClass.serializerOrNull()
            ?: throw IllegalArgumentException("No serializer for ${bodyClass.simpleName}")

    @Throws(Error::class)
    suspend fun handle(
        ctx: InternalNodeContext,
        rawMessage: RawMessage,
        json: Json,
    ) {
        val body =
            json
                .decodeFromJsonElement(deserializer, rawMessage.body)
                .let { bodyClass.cast(it) }

        require(body.type == messageType)

        val message = Message(rawMessage.src, rawMessage.dst, body)

        @Suppress("UNCHECKED_CAST")
        (handler as Handler<Body>).handle(ctx, message)
    }
}

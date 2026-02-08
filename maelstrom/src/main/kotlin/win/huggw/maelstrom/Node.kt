package win.huggw.maelstrom

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import win.huggw.maelstrom.error.MaelstromError
import win.huggw.maelstrom.error.NotSupportedError
import win.huggw.maelstrom.handler.GeneralHandler
import win.huggw.maelstrom.message.BaseBody
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.Message
import win.huggw.maelstrom.message.MessageType
import win.huggw.maelstrom.message.RawMessage

class Node internal constructor(
    internal val handlers: Map<MessageType, GeneralHandler>,
    internal val json: Json,
) {
    private val reader = System.`in`.bufferedReader()
    private val writer = System.out.bufferedWriter()
    private val logger = System.err.bufferedWriter()

    fun listen() {
        while (true) {
            val (messageType, rawMessage) =
                runCatching { receiveMessage() }
                    .onFailure {
                        log("Internal Error: ${it.message}")
                        continue
                    }.getOrNull() ?: continue

            val handler =
                handlers[messageType]
                    ?: let {
                        pushError(
                            NotSupportedError("Message type $messageType is not supported."),
                            rawMessage,
                        )
                        continue
                    }

            runCatching {
                handler.handle(rawMessage, json)
            }.onFailure {
                when (it) {
                    is MaelstromError -> pushError(it, rawMessage)
                }
            }
        }
    }

    private fun receiveMessage(): Pair<MessageType, RawMessage>? {
        val line = reader.readLine() ?: return null
        val rawMessage = json.decodeFromString<RawMessage>(line)

        val messageType =
            checkNotNull(rawMessage.body.jsonObject["type"])
                .jsonPrimitive.content

        return messageType to rawMessage
    }

    private fun pushError(
        error: MaelstromError,
        rawMessage: RawMessage,
    ) {
        // try to extract msg id from raw message
        if (error.fromMsgId == null) {
            runCatching {
                json.decodeFromJsonElement<BaseBody>(rawMessage.body)
            }.onSuccess {
                error.fromMsgId = it.msgId
            }
        }

        val errorBody = error.toErrorMessageBody()
        val errorMessage = rawMessage.replyTo(json.encodeToJsonElement(errorBody))

        pushMessage(errorMessage)
    }

    private inline fun <reified B : Body> pushMessage(message: Message<B>) {
        // TODO: make asynchronous
        writer.write(json.encodeToString(message) + "\n")
        writer.flush()
    }

    private fun pushMessage(rawMessage: RawMessage) {
        // TODO: make asynchronous
        writer.write(json.encodeToString(rawMessage) + "\n")
        writer.flush()
    }

    private fun log(message: String) {
        // TODO: make asynchronous
        logger.write("$message\n")
        logger.flush()
    }
}

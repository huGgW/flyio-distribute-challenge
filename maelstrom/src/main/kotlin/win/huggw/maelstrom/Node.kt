package win.huggw.maelstrom

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.json.*
import win.huggw.maelstrom.error.MaelstromError
import win.huggw.maelstrom.error.NotSupportedError
import win.huggw.maelstrom.handler.GeneralHandler
import win.huggw.maelstrom.message.*

class Node internal constructor(
    internal val handlers: Map<MessageType, GeneralHandler>,
    internal val json: Json,
) {
    private val reader = System.`in`.bufferedReader()
    private val writer = System.out.bufferedWriter()
    private val logger = System.err.bufferedWriter()

    suspend fun listen() = coroutineScope {
        val messageReceiveChan = Channel<Pair<MessageType, RawMessage>>()
        val messageSendChan = Channel<RawMessage>()

        val receiveMessageJob = launch { receiveMessageLoop(messageReceiveChan) }
        val sendMessageJob = launch { sendMessageLoop(messageSendChan) }

        val handlerJobs = mutableListOf<Job>()
        while (true) {
            val (messageType, rawMessage) = messageReceiveChan.receiveCatching().getOrNull()
                ?: break

            handlerJobs.add(
                launch {
                    val handler =
                        handlers[messageType]
                            ?: let {
                                pushError(
                                    NotSupportedError("Message type $messageType is not supported."),
                                    rawMessage,
                                )
                                return@launch
                            }

                    runCatching {
                        handler.handle(rawMessage, json)
                    }.onFailure {
                        when (it) {
                            is MaelstromError -> pushError(it, rawMessage)
                        }
                    }
                }
            )
        }

        handlerJobs.apply {
            add(receiveMessageJob)
            add(sendMessageJob)
        }.joinAll()
    }

    // TODO: seperate io and parse, and use concurrent parsing

    private suspend fun receiveMessageLoop(messageReceiveChan: SendChannel<Pair<MessageType, RawMessage>>) {
        while (true) {
            val messagePair = runCatching {
                receiveMessage()
            }.onFailure {
                log("Internal Error: ${it.message}")
            }.getOrNull() ?: continue

            runCatching {
                messageReceiveChan.send(messagePair)
            }.onFailure {
                if (it !is ClosedSendChannelException) {
                    log("Internal Error: ${it.message}")
                }
            }
        }
    }

    private suspend fun receiveMessage(): Pair<MessageType, RawMessage>? =
        withContext(Dispatchers.IO) {
            val line = reader.readLine() ?: return@withContext null
            val rawMessage = json.decodeFromString<RawMessage>(line)

            val messageType =
                checkNotNull(rawMessage.body.jsonObject["type"])
                    .jsonPrimitive.content

            messageType to rawMessage
        }

    private suspend inline fun <reified B : Body> pushMessage(
        message: Message<B>,
        sendChan: SendChannel<RawMessage>
    ) {
        val rawBody = json.encodeToJsonElement(message.body)
        val rawMessage = RawMessage(
            src = message.dst,
            dst = message.src,
            body = rawBody,
        )

        runCatching {
            sendChan.send(rawMessage)
        }.onFailure {
            if (it !is ClosedSendChannelException) {
                log("Internal Error: ${it.message}")
            }
        }
    }

    private suspend fun pushError(
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

        runCatching {
            sendMessage(errorMessage)
        }.onFailure {
            if (it !is ClosedSendChannelException) {
                log("Internal Error: ${it.message}")
            }
        }
    }

    private suspend fun sendMessageLoop(messageSendChan: ReceiveChannel<RawMessage>) {
        for (message in messageSendChan) {
            sendMessage(message)
        }
    }

    private suspend fun sendMessage(rawMessage: RawMessage) = withContext(Dispatchers.IO) {
        writer.write(json.encodeToString(rawMessage) + "\n")
        writer.flush()
    }

    private suspend fun log(message: String) = withContext(Dispatchers.IO) {
        logger.write("$message\n")
        logger.flush()
    }
}

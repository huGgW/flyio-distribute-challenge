package win.huggw.maelstrom

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.json.*
import win.huggw.maelstrom.error.CrashError
import win.huggw.maelstrom.error.MaelstromError
import win.huggw.maelstrom.error.MalformedRequestError
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
        val messageReceiveChan = Channel<String>()
        val messageSendChan = Channel<String>()

        val receiveMessageJob = launch { receiveMessageLoop(messageReceiveChan) }
        val sendMessageJob = launch { sendMessageLoop(messageSendChan) }

        val handlerJobs = mutableListOf<Job>()
        while (true) {
            val line = messageReceiveChan.receiveCatching().getOrNull() ?: break

            handlerJobs.add(
                launch {
                    val (messageType, rawMessage) = runCatching {
                        parseMessage(line)
                    }.onFailure {
                        // TODO: make error pushable
                        log("Invalid message format: $line")
                    }.getOrNull() ?: return@launch

                    val handler =
                        handlers[messageType]
                            ?: let {
                                pushError(
                                    NotSupportedError("Message type $messageType is not supported."),
                                    rawMessage,
                                    messageSendChan,
                                )
                                return@launch
                            }

                    runCatching {
                        handler.handle(rawMessage, json)
                    }.onFailure {
                        when (it) {
                            is MaelstromError -> pushError(it, rawMessage, messageSendChan)
                            else -> {
                                log("Handling Internal Error: ${rawMessage to it}")
                                pushError(
                                    CrashError(
                                        text = "Unexpected error: ${it.message}"
                                    ),
                                    rawMessage,
                                    messageSendChan,
                                )
                            }
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

    private suspend fun receiveMessageLoop(messageReceiveChan: SendChannel<String>) = withContext(Dispatchers.IO) {
        while (true) {
            val line = reader.readLine() ?: break

            runCatching {
                messageReceiveChan.send(line)
            }.onFailure {
                if (it !is ClosedSendChannelException) {
                    log("Internal Error: ${it.message}")
                }
            }
        }
    }

    private suspend fun sendMessageLoop(messageSendChan: ReceiveChannel<String>) = withContext(Dispatchers.IO) {
        for (line in messageSendChan) {
            writer.write(line + "\n")
            writer.flush()
        }
    }

    private fun parseMessage(line: String): Pair<MessageType, RawMessage> {
        val rawMessage = json.decodeFromString<RawMessage>(line)
        val messageType =
            checkNotNull(rawMessage.body.jsonObject["type"])
                .jsonPrimitive.content

        return messageType to rawMessage
    }

    private suspend inline fun <reified B : Body> pushMessage(
        message: Message<B>,
        sendChan: SendChannel<String>,
    ) {
        val rawBody = json.encodeToJsonElement(message.body)
        val rawMessage = RawMessage(
            src = message.dst,
            dst = message.src,
            body = rawBody,
        )
        val line = json.encodeToString(rawMessage)

        runCatching {
            sendChan.send(line)
        }.onFailure {
            if (it !is ClosedSendChannelException) {
                log("Internal Error: ${it.message}")
            }
        }
    }

    private suspend fun pushError(
        error: MaelstromError,
        rawMessage: RawMessage,
        sendChan: SendChannel<String>,
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

        val line = json.encodeToString(errorMessage)

        runCatching {
            sendChan.send(line)
        }.onFailure {
            if (it !is ClosedSendChannelException) {
                log("Internal Error: ${it.message}")
            }
        }
    }

    private suspend fun log(message: String) = withContext(Dispatchers.IO) {
        logger.write("$message\n")
        logger.flush()
    }
}

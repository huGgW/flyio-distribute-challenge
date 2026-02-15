package win.huggw.maelstrom.node

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import win.huggw.maelstrom.error.CrashError
import win.huggw.maelstrom.error.MaelstromError
import win.huggw.maelstrom.error.NotSupportedError
import win.huggw.maelstrom.error.TemporarilyUnavailableError
import win.huggw.maelstrom.handler.GeneralHandler
import win.huggw.maelstrom.init.INIT_MESSAGE_TYPE
import win.huggw.maelstrom.message.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass

class Node internal constructor(
    internal val handlers: Map<MessageType, GeneralHandler>,
    internal val json: Json,
    private val reader: BufferedReader = System.`in`.bufferedReader(),
    private val writer: BufferedWriter = System.out.bufferedWriter(),
    private val logger: BufferedWriter = System.err.bufferedWriter(),
) {
    private val id: AtomicReference<String?> = AtomicReference(null)
    private val messageReceiveChan = Channel<String>()
    private val messageSendChan = Channel<String>()

    suspend fun listen() = coroutineScope {

        val receiveMessageJob = launch { receiveMessageLoop() }
        val sendMessageJob = launch { sendMessageLoop() }

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

                    if (messageType != INIT_MESSAGE_TYPE && id.get() == null) {
                        pushError(
                            TemporarilyUnavailableError(
                                text = "Node not initialized.",
                            ),
                            rawMessage,
                        )
                        return@launch
                    }

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
                        handler.handle(context(), rawMessage, json)
                    }.onFailure {
                        when (it) {
                            is MaelstromError -> pushError(it, rawMessage)
                            else -> {
                                log("Handling Internal Error: ${rawMessage to it}")
                                pushError(
                                    CrashError(
                                        text = "Unexpected error: ${it.message}"
                                    ),
                                    rawMessage,
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

    private suspend fun receiveMessageLoop() = withContext(Dispatchers.IO) {
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

    private suspend fun sendMessageLoop() = withContext(Dispatchers.IO) {
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

    @OptIn(InternalSerializationApi::class)
    private suspend fun <B: Body> pushMessage(
        message: Message<B>,
        bodyClass: KClass<B>,
    ) {
        val rawBody = json.encodeToJsonElement(bodyClass.serializer(), message.body)
        val rawMessage = RawMessage(
            src = message.dst,
            dst = message.src,
            body = rawBody,
        )
        val line = json.encodeToString(rawMessage)

        runCatching {
            messageSendChan.send(line)
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

        val line = json.encodeToString(errorMessage)

        runCatching {
            messageSendChan.send(line)
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

    internal fun context() = let {
        object: InternalNodeContext {
            override fun setId(id: String) {
                if (!it.id.compareAndSet(null, id)) {
                    error("Node ID already set.")
                }
            }

            override val id: String
                get() = it.id.get() ?: error("Node not initialized.")

            override suspend fun <B: Body> push(message: Message<B>, bodyClass: KClass<B>) {
                it.pushMessage(
                    message,
                    bodyClass,
                )
            }

            override suspend fun log(message: String) {
                it.log(message)
            }
        }
    }
}

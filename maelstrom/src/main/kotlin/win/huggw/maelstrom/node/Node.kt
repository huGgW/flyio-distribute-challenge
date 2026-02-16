package win.huggw.maelstrom.node

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer
import win.huggw.maelstrom.error.CrashError
import win.huggw.maelstrom.error.MaelstromError
import win.huggw.maelstrom.error.NotSupportedError
import win.huggw.maelstrom.error.TemporarilyUnavailableError
import win.huggw.maelstrom.handler.GeneralHandler
import win.huggw.maelstrom.init.INIT_MESSAGE_TYPE
import win.huggw.maelstrom.message.BaseBody
import win.huggw.maelstrom.message.Body
import win.huggw.maelstrom.message.Message
import win.huggw.maelstrom.message.MessageType
import win.huggw.maelstrom.message.RawMessage
import java.io.BufferedReader
import java.io.BufferedWriter
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement
import kotlin.reflect.KClass

@OptIn(ExperimentalAtomicApi::class)
class Node internal constructor(
    internal val handlers: Map<MessageType, GeneralHandler>,
    internal val json: Json,
    private val reader: BufferedReader = System.`in`.bufferedReader(),
    private val writer: BufferedWriter = System.out.bufferedWriter(),
    private val logger: BufferedWriter = System.err.bufferedWriter(),
) {
    private val id: AtomicReference<String?> = AtomicReference(null)
    private val nodeIds = AtomicReference<Set<String>?>(null)
    private val latestMsgId = AtomicInt(0)

    private val messageReceiveChan = Channel<String>()
    private val messageSendChan = Channel<String>()

    suspend fun listen() =
        coroutineScope {
            val receiveMessageJob = launch { receiveMessageLoop() }
            val sendMessageJob = launch { sendMessageLoop() }

            val handlerJobs = mutableListOf<Job>()
            while (true) {
                val line = messageReceiveChan.receiveCatching().getOrNull() ?: break

                handlerJobs.add(
                    launch {
                        val (messageType, rawMessage) =
                            runCatching { parseMessage(line) }
                                .onSuccess { log("Received message: $line") }
                                .onFailure { log("Invalid message format: $line") } // TODO: make error pushable
                                .getOrNull() ?: return@launch

                        if (messageType != INIT_MESSAGE_TYPE && id.load() == null) {
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
                                is MaelstromError -> {
                                    pushError(it, rawMessage)
                                }

                                else -> {
                                    log("Handling Internal Error: ${rawMessage to it}")
                                    pushError(
                                        CrashError(
                                            text = "Unexpected error: ${it.message}",
                                        ),
                                        rawMessage,
                                    )
                                }
                            }
                        }
                    },
                )
            }

            handlerJobs
                .apply {
                    add(receiveMessageJob)
                    add(sendMessageJob)
                }.joinAll()
        }

    private suspend fun receiveMessageLoop() =
        withContext(Dispatchers.IO) {
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

    private suspend fun sendMessageLoop() =
        withContext(Dispatchers.IO) {
            for (line in messageSendChan) {
                log("Sending message: $line")
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
    private suspend fun <B : Body> pushMessage(
        message: Message<B>,
        bodyClass: KClass<B>,
    ) {
        val rawBody = json.encodeToJsonElement(bodyClass.serializer(), message.body)
        val rawMessage =
            RawMessage(
                src = message.src,
                dest = message.dest,
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

    private suspend fun log(message: String) =
        withContext(Dispatchers.IO) {
            logger.write("$message\n")
            logger.flush()
        }

    private fun nextMessageId() = latestMsgId.fetchAndIncrement()

    internal fun context() =
        let {
            object : InitNodeContext {
                override val id: String
                    get() = it.id.load() ?: error("Node not initialized.")

                override fun setId(id: String) {
                    if (!it.id.compareAndSet(null, id)) {
                        error("Node ID already set.")
                    }
                }

                override val nodeIds: Set<String>
                    get() = it.nodeIds.load() ?: error("Node IDs not initialized.")

                override fun setNodeIds(nodeIds: Set<String>) {
                    if (!it.nodeIds.compareAndSet(null, nodeIds)) {
                        error("Node IDs already set.")
                    }
                }

                override fun nextMessageId() = it.nextMessageId()

                override suspend fun <B : Body> push(
                    message: Message<B>,
                    bodyClass: KClass<B>,
                ) {
                    it.pushMessage(message, bodyClass)
                }

                override suspend fun log(message: String) = it.log(message)
            }
        }
}

package win.huggw.app.counter.seqkv

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import win.huggw.app.counter.seqkv.message.*
import win.huggw.app.counter.seqkv.message.*
import win.huggw.maelstrom.error.KeyDoesNotExistError
import win.huggw.maelstrom.error.PreconditionFailedError
import win.huggw.maelstrom.handler.Handler
import win.huggw.maelstrom.message.Message
import win.huggw.maelstrom.message.MessageType
import win.huggw.maelstrom.node.NodeContext
import win.huggw.maelstrom.node.rpc
import win.huggw.maelstrom.node.send

class SeqKVHandler(
    private val repository: Repository,
) {
    val readHandler = object : Handler<ReadBody> {
        override val messageType: MessageType = READ_MESSAGE_TYPE

        override suspend fun handle(ctx: NodeContext, message: Message<ReadBody>) {
            val key = message.body.key

            repository.get(key)
                ?.let {
                    ctx.send(
                        message.replyTo(
                            message.body.reply(it, ctx.nextMessageId())
                        )
                    )
                }
                ?: throw KeyDoesNotExistError(
                    "Key does not exist: $key",
                    message.body.msgId,
                )
        }
    }

    val writeHandler = object : Handler<WriteBody> {
        override val messageType = WRITE_MESSAGE_TYPE

        override suspend fun handle(ctx: NodeContext, message: Message<WriteBody>) {
            val key = message.body.key
            val value = message.body.value

            repository.put(key, value)

            propagateWrite(ctx, key, value)

            ctx.send(
                message.replyTo(
                    message.body.reply(ctx.nextMessageId())
                )
            )
        }
    }

    val compareAndSwapHandler = object : Handler<CompareAndSwapBody> {
        override val messageType = CAS_MESSAGE_TYPE

        override suspend fun handle(ctx: NodeContext, message: Message<CompareAndSwapBody>) {
            val key = message.body.key
            val from = message.body.from
            val to = message.body.to

            val swapped = repository.compareAndSwap(key, from, to)
            if (swapped) {
                ctx.send(
                    message.replyTo(
                        message.body.reply(ctx.nextMessageId())
                    )
                )
                return
            }

            // check key exists for error
            val storedValue = repository.get(key)
            if (storedValue == null) {
                throw KeyDoesNotExistError(
                    "Key does not exist: $key",
                    message.body.msgId,
                )
            } else {
                throw PreconditionFailedError(
                    "Key has diffeent value: $storedValue",
                    message.body.msgId,
                )
            }
        }
    }

    private suspend fun vote(ctx: NodeContext, key: String, value: Int?): Boolean {
        val agreementMin = Math.ceilDiv(ctx.nodeIds.size, 2)

        val reads = supervisorScope {
            ctx.nodeIds
                .filter { it != ctx.id }
                .map { id ->
                    async {
                        val defer = ctx.rpc(
                            Message<ReadBody>(
                                src = ctx.id,
                                dest = id,
                                body = ReadBody(key, ctx.nextMessageId())
                            )
                        )

                        val response = defer.await()

                        when (val body = response.body) {
                            is ReadOkBody -> body.value
                            else -> null
                        }
                    }
                }
        }.awaitAll()

        val agreements = reads.count { it == value } + 1

        return agreements >= agreementMin
    }


    private suspend fun propagateWrite(ctx: NodeContext, key: String, value: Int) {
        supervisorScope {
            ctx.nodeIds.filter { it != ctx.id }
            .forEach { id ->
                launch {
                    ctx.send(
                        Message<WriteBody>(
                            src = ctx.id,
                            dest = id,
                            body = WriteBody(
                                key,
                                value,
                                ctx.nextMessageId(),
                            )
                        )
                    )
                }
            }
        }
    }
}


package win.huggw.app.broadcast

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.time.withTimeoutOrNull
import win.huggw.app.broadcast.read.ReadBody
import win.huggw.app.broadcast.read.ReadOkBody
import win.huggw.maelstrom.message.Message
import win.huggw.maelstrom.node.NodeContext
import win.huggw.maelstrom.node.rpc
import java.time.Duration
import kotlin.random.Random

class Poller(
    private val repository: Repository,
    private val ctx: NodeContext,
    private val minBackoff: Duration = Duration.ofMillis(100),
    private val maxBackoff: Duration = Duration.ofMillis(500),
    private val ignoreTopology: Boolean = false,
) {
    suspend fun loop() {
        while (true) {
            val current =
                runCatching {
                    ctx.id
                }.onFailure {
                    // only run loop after node initialized
                    backoff()
                    continue
                }.getOrNull()!!

            val neighbors =
                if (ignoreTopology) {
                    ctx.nodeIds - current
                } else {
                    repository.getTopology()[current]
                        ?: (ctx.nodeIds - current)
                }

            val currentMessages = repository.getMessages()

            val neighborMessagesMap: Map<String, Set<Int>> =
                supervisorScope {
                    neighbors.map {
                        async {
                            val responseDeferred =
                                ctx.rpc(
                                    Message(
                                        src = current,
                                        dest = it,
                                        body =
                                            ReadBody(
                                                msgId = ctx.nextMessageId(),
                                            ),
                                    ),
                                )

                            val body =
                                withTimeoutOrNull(Duration.ofMillis(100)) { responseDeferred.await().body }
                                    ?: return@async it to null

                            when (body) {
                                is ReadOkBody -> it to body.messages.toSet()
                                else -> it to null
                            }
                        }
                    }
                }.awaitAll()
                    .associate { (n, m) -> n to (m ?: emptySet()) }

            val completedMessages =
                neighborMessagesMap.values
                    .fold(currentMessages) { acc, messages -> acc.union(messages) }

            val currentShortages = completedMessages - currentMessages
            if (currentShortages.isNotEmpty()) {
                repository.addMessages(currentShortages)
            }

            backoff()
        }
    }

    suspend fun backoff() {
        kotlinx.coroutines.delay(
            Random.nextLong(minBackoff.toMillis(), maxBackoff.toMillis()),
        )
    }
}

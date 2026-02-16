package win.huggw.app.broadcast

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.read
import kotlin.concurrent.write

@OptIn(ExperimentalAtomicApi::class)
class Repository {
    private val messages = hashSetOf<Int>()
    private val messageLocker = ReentrantReadWriteLock()
    private val topology = AtomicReference<Map<String, Set<String>>>(emptyMap())

    fun addMessage(message: Int): Boolean {
        messageLocker.read {
            if (message in messages) {
                return false
            }

            messageLocker.write {
                if (message in messages) {
                    return false
                }

                messages.add(message)
                return true
            }
        }
    }

    fun getMessages(): List<Int> = messageLocker.read { messages.toList() }

    fun updateTopology(topology: Map<String, Set<String>>) {
        this.topology.store(topology)
    }

    fun getTopology() = topology.load()
}

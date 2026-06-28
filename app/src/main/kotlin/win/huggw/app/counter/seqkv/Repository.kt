package win.huggw.app.counter.seqkv

import java.util.concurrent.ConcurrentHashMap

class Repository {
    private val map = ConcurrentHashMap<String, Int>()

    fun get(key: String): Int? = map[key]

    fun put(key: String, value: Int) {
        map[key] = value
    }

    fun compareAndSwap(key: String, from: Int, to: Int): Boolean = map.replace(key, from, to)
}

package win.huggw.app.uniqueid

import kotlin.random.Random
import kotlin.time.Clock

fun generate(nodeId: String, replyMsgId: Int): String {
        // given nodeId, replyMsgId will ensure generate id is unique in current nodes

        // currentMillis will ensure generate id will defer from past node which has same node id
        val currentMillis = Clock.System.now().toEpochMilliseconds()

        // this part add randomness for just in case
        val randHex = Random.nextInt().toString(16)

        // created id will be sorted by time
        return "$currentMillis-$nodeId-$replyMsgId-$randHex"
}

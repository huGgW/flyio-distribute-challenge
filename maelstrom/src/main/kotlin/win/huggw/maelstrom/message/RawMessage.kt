package win.huggw.maelstrom.message

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal class RawMessage(
    val src: String,
    val dst: String,
    val body: JsonElement,
) {
    fun replyTo(replyBody: JsonElement) =
        RawMessage(
            src = dst,
            dst = src,
            body = replyBody,
        )
}

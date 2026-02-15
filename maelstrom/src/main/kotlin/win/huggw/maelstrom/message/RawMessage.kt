package win.huggw.maelstrom.message

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal class RawMessage(
    val src: String,
    val dest: String,
    val body: JsonElement,
) {
    fun replyTo(replyBody: JsonElement) =
        RawMessage(
            src = dest,
            dest = src,
            body = replyBody,
        )
}

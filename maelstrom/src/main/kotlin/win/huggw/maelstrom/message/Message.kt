package win.huggw.maelstrom.message

import kotlinx.serialization.Serializable

@Serializable
data class Message<B : Body>(
    val src: String,
    val dest: String,
    val body: B,
) {
    fun <B : Body> replyTo(replyBody: B) =
        Message(
            src = dest,
            dest = src,
            body = replyBody,
        )
}

package win.huggw.maelstrom.error

sealed class MaelstromError(
    val code: Int,
    val name: String,
    val definite: Boolean = false,
    val text: String? = null,
    var fromMsgId: Int? = null,
) : Exception("$code: ${text ?: name}") {
    fun toErrorMessageBody() =
        ErrorMessageBody(
            name = name,
            code = code,
            text = text,
            inReplyTo = fromMsgId,
        )
}

abstract class CustomMaelstromError(
    code: Int,
    name: String,
    definite: Boolean = false,
    text: String? = null,
    fromMsgId: Int? = null,
): MaelstromError(code, name, definite, text, fromMsgId) {
    init {
        require(code > 100)
    }
}

sealed class InternalMaelstromError(
    code: Int,
    name: String,
    definite: Boolean = false,
    text: String? = null,
    fromMsgId: Int? = null,
): MaelstromError(code, name, definite, text, fromMsgId) {
    init {
        require(code in 0..<100)
    }
}

class TimeoutError(
    text: String? = null,
    fromMsgId: Int? = null,
) : MaelstromError(0, "timeout", false, text)

class NodeNotFoundError(
    text: String? = null,
    fromMsgId: Int? = null,
) : MaelstromError(1, "node-not-found", true, text)

class NotSupportedError(
    text: String? = null,
    fromMsgId: Int? = null,
) : MaelstromError(10, "not-supported", true, text)

class TemporarilyUnavailableError(
    text: String? = null,
    fromMsgId: Int? = null,
) : MaelstromError(11, "temporarily-unavailable", true, text)

class MalformedRequestError(
    text: String? = null,
    fromMsgId: Int? = null,
) : MaelstromError(12, "malformed-request", true, text)

class CrashError(
    text: String? = null,
    fromMsgId: Int? = null,
) : MaelstromError(13, "crash", false, text)

class AbortError(
    text: String? = null,
    fromMsgId: Int? = null,
) : MaelstromError(14, "abort", true, text)

class KeyDoesNotExistError(
    text: String? = null,
    fromMsgId: Int? = null,
) : MaelstromError(20, "key-does-not-exist", true, text)

class KeyAlreadyExistsError(
    text: String? = null,
    fromMsgId: Int? = null,
) : MaelstromError(21, "key-already-exists", true, text)

class PreconditionFailedError(
    text: String? = null,
    fromMsgId: Int? = null,
) : MaelstromError(22, "precondition-failed", true, text)

class TxnConflictError(
    text: String? = null,
    fromMsgId: Int? = null,
) : MaelstromError(30, "txn-conflict", true, text)

package win.huggw.maelstrom

abstract class Error(
    val code: Int,
    val name: String,
    val definite: Boolean = false,
    val text: String? = null,
) : Exception("$code: ${text ?: name}")

class TimeoutError(
    text: String? = null,
) : Error(0, "timeout", false, text)

class NodeNotFoundError(
    text: String? = null,
) : Error(1, "node-not-found", true, text)

class NotSupportedError(
    text: String? = null,
) : Error(10, "not-supported", true, text)

class TemporarilyUnavailableError(
    text: String? = null,
) : Error(11, "temporarily-unavailable", true, text)

class MalformedRequestError(
    text: String? = null,
) : Error(12, "malformed-request", true, text)

class CrashError(
    text: String? = null,
) : Error(13, "crash", false, text)

class AbortError(
    text: String? = null,
) : Error(14, "abort", true, text)

class KeyDoesNotExistError(
    text: String? = null,
) : Error(20, "key-does-not-exist", true, text)

class KeyAlreadyExistsError(
    text: String? = null,
) : Error(21, "key-already-exists", true, text)

class PreconditionFailedError(
    text: String? = null,
) : Error(22, "precondition-failed", true, text)

class TxnConflictError(
    text: String? = null,
) : Error(30, "txn-conflict", true, text)

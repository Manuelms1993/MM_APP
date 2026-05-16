package com.example.mmapp.remote

sealed class RemoteSyncException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    class InvalidRepositoryConfig(
        message: String,
        cause: Throwable? = null,
    ) : RemoteSyncException(message, cause)

    class NetworkUnavailable(
        message: String,
        cause: Throwable? = null,
    ) : RemoteSyncException(message, cause)

    class Timeout(
        message: String,
        cause: Throwable? = null,
    ) : RemoteSyncException(message, cause)

    class HttpError(
        val statusCode: Int,
        message: String,
        cause: Throwable? = null,
    ) : RemoteSyncException(message, cause)

    class InvalidRemoteContent(
        message: String,
        cause: Throwable? = null,
    ) : RemoteSyncException(message, cause)

    class EmptyRemoteIndex(
        message: String,
        cause: Throwable? = null,
    ) : RemoteSyncException(message, cause)
}

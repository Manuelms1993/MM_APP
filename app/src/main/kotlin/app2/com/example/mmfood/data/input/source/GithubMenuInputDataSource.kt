package com.example.mmapp.app2.data.input.source

import com.example.mmapp.app2.data.input.config.MenuInputRepositoryConfig
import com.example.mmapp.remote.RemoteSyncException
import kotlinx.serialization.SerializationException
import java.net.HttpURLConnection
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class RemoteMenuFile(
    val fileName: String,
    val sha: String,
    val downloadUrl: String,
)

class GithubMenuInputDataSource(
    private val config: MenuInputRepositoryConfig,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun loadRemoteFiles(): List<RemoteMenuFile> = runRemoteOperation {
        val apiUrl = config.inputsRepositoryTreeUrl.toGithubContentsApiUrl()
        val response = openGetConnection(apiUrl)

        response.useSuccessfulInputStream { body ->
            val files = try {
                json.parseToJsonElement(body).jsonArray
                    .mapNotNull { element ->
                        val item = element.jsonObject
                        val fileName = item["name"]?.jsonPrimitive?.content
                        val sha = item["sha"]?.jsonPrimitive?.content
                        val downloadUrl = item["download_url"]?.jsonPrimitive?.content
                        if (
                            fileName.isNullOrBlank() ||
                            sha.isNullOrBlank() ||
                            downloadUrl.isNullOrBlank() ||
                            fileName !in config.expectedFileNames
                        ) {
                            null
                        } else {
                            RemoteMenuFile(fileName, sha, downloadUrl)
                        }
                    }
                    .sortedBy { it.fileName.lowercase() }
            } catch (exception: Exception) {
                throw RemoteSyncException.InvalidRemoteContent(
                    message = "No se pudo interpretar el índice remoto de menús.",
                    cause = exception,
                )
            }

            if (files.isEmpty()) {
                throw RemoteSyncException.EmptyRemoteIndex(
                    "No se encontraron los JSON de menú esperados en el repositorio remoto.",
                )
            }
            files
        }
    }

    fun downloadRawJson(downloadUrl: String): String = runRemoteOperation {
        openGetConnection(downloadUrl).useSuccessfulInputStream { it }
    }

    private fun openGetConnection(url: String): HttpURLConnection = try {
        URL(url).openConnection().let { connection ->
            connection as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection
        }
    } catch (exception: Exception) {
        throw exception.toRemoteSyncException()
    }

    private fun <T> HttpURLConnection.useSuccessfulInputStream(block: (String) -> T): T {
        return try {
            if (responseCode !in 200..299) {
                throw RemoteSyncException.HttpError(
                    statusCode = responseCode,
                    message = "GitHub devolvió HTTP $responseCode.",
                )
            }
            inputStream.bufferedReader().use { reader -> block(reader.readText()) }
        } finally {
            disconnect()
        }
    }

    private fun String.toGithubContentsApiUrl(): String {
        try {
            val prefix = "https://github.com/"
            require(startsWith(prefix)) { "GitHub tree URL inválida: $this" }
            val segments = removePrefix(prefix).split("/")
            require(segments.size >= 5 && segments[2] == "tree") { "GitHub tree URL inválida: $this" }
            val owner = segments[0]
            val repo = segments[1]
            val branch = segments[3]
            val path = segments.drop(4).joinToString("/")
            return "https://api.github.com/repos/$owner/$repo/contents/$path?ref=$branch"
        } catch (exception: IllegalArgumentException) {
            throw RemoteSyncException.InvalidRepositoryConfig(
                message = "La URL del repositorio de menús no es válida.",
                cause = exception,
            )
        }
    }

    private fun <T> runRemoteOperation(block: () -> T): T = try {
        block()
    } catch (exception: RemoteSyncException) {
        throw exception
    } catch (exception: Exception) {
        throw exception.toRemoteSyncException()
    }

    private fun Exception.toRemoteSyncException(): RemoteSyncException = when (this) {
        is RemoteSyncException -> this
        is UnknownHostException, is ConnectException, is NoRouteToHostException, is SSLException ->
            RemoteSyncException.NetworkUnavailable(
                message = "No hay conexión disponible para sincronizar menús.",
                cause = this,
            )

        is SocketTimeoutException ->
            RemoteSyncException.Timeout(
                message = "La sincronización de menús ha agotado el tiempo de espera.",
                cause = this,
            )

        is SerializationException ->
            RemoteSyncException.InvalidRemoteContent(
                message = "El contenido remoto de menús no tiene un JSON válido.",
                cause = this,
            )

        else ->
            RemoteSyncException.InvalidRemoteContent(
                message = "No se pudo leer el contenido remoto de menús.",
                cause = this,
            )
    }
}

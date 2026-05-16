package com.example.plantreminder.data.input.source

import com.example.plantreminder.data.input.config.PlantInputRepositoryConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL

data class RemotePlantFile(
    val fileName: String,
    val sha: String,
    val downloadUrl: String,
)

class GithubPlantInputDataSource(
    private val config: PlantInputRepositoryConfig,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun loadRemoteFiles(): List<RemotePlantFile> = runCatching {
        val apiUrl = config.inputsRepositoryTreeUrl.toGithubContentsApiUrl()
        val response = URL(apiUrl).openConnection().let { connection ->
            connection as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection
        }

        response.useSuccessfulInputStream { body ->
            json.parseToJsonElement(body).jsonArray
                .mapNotNull { element ->
                    val item = element.jsonObject
                    val fileName = item["name"]?.jsonPrimitive?.content
                    val sha = item["sha"]?.jsonPrimitive?.content
                    val downloadUrl = item["download_url"]?.jsonPrimitive?.content
                    if (fileName.isNullOrBlank() || sha.isNullOrBlank() || downloadUrl.isNullOrBlank() || !fileName.endsWith(".json", ignoreCase = true)) {
                        null
                    } else {
                        RemotePlantFile(fileName, sha, downloadUrl)
                    }
                }
                .sortedBy { it.fileName.lowercase() }
        }
    }.getOrDefault(emptyList())

    fun downloadRawJson(downloadUrl: String): String {
        val connection = URL(downloadUrl).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        return connection.useSuccessfulInputStream { it }
    }

    private fun <T> HttpURLConnection.useSuccessfulInputStream(block: (String) -> T): T {
        return try {
            if (responseCode !in 200..299) error("HTTP $responseCode")
            inputStream.bufferedReader().use { reader -> block(reader.readText()) }
        } finally {
            disconnect()
        }
    }

    private fun String.toGithubContentsApiUrl(): String {
        val prefix = "https://github.com/"
        require(startsWith(prefix)) { "GitHub tree URL inválida: $this" }
        val segments = removePrefix(prefix).split("/")
        require(segments.size >= 5 && segments[2] == "tree") { "GitHub tree URL inválida: $this" }
        val owner = segments[0]
        val repo = segments[1]
        val branch = segments[3]
        val path = segments.drop(4).joinToString("/")
        return "https://api.github.com/repos/$owner/$repo/contents/$path?ref=$branch"
    }
}

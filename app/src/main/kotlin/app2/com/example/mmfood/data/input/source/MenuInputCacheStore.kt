package com.example.mmapp.app2.data.input.source

import android.content.Context
import com.example.mmapp.app2.data.input.RawMenuInput
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class CachedRemoteFile(
    val fileName: String,
    val sha: String,
)

data class CachedMenuFile(
    val fileName: String,
    val sha: String,
)

class MenuInputCacheStore(
    private val context: Context,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun readInputs(): List<RawMenuInput> {
        val files = cacheDirectory().listFiles().orEmpty()
            .filter { it.isFile && it.name.endsWith(".json", ignoreCase = true) }
            .sorted()

        return files.mapNotNull { file ->
            runCatching {
                RawMenuInput(
                    fileName = file.name,
                    rawJson = file.readText(),
                )
            }.getOrNull()
        }
    }

    fun readMetadata(): List<CachedMenuFile> {
        val file = metadataFile()
        if (!file.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<List<CachedRemoteFile>>(file.readText())
                .map { CachedMenuFile(it.fileName, it.sha) }
        }.getOrDefault(emptyList())
    }

    fun writeMenuFile(fileName: String, rawJson: String) {
        val directory = cacheDirectory().also { if (!it.exists()) it.mkdirs() }
        File(directory, fileName).writeText(rawJson)
    }

    fun deleteMissingFiles(validNames: Set<String>): Int {
        val removedFiles = cacheDirectory().listFiles().orEmpty()
            .filter { it.isFile && it.name.endsWith(".json", ignoreCase = true) && it.name !in validNames }
        removedFiles.forEach { it.delete() }
        return removedFiles.size
    }

    fun writeMetadata(files: List<CachedMenuFile>) {
        val directory = cacheDirectory().also { if (!it.exists()) it.mkdirs() }
        File(directory, CACHE_METADATA_FILE_NAME).writeText(
            json.encodeToString(files.map { CachedRemoteFile(it.fileName, it.sha) }),
        )
    }

    fun shouldUseCachedInputs(): Boolean {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val appLastUpdateTime = packageInfo.lastUpdateTime
        val metadataLastModified = metadataFile().takeIf { it.exists() }?.lastModified() ?: return false
        return metadataLastModified >= appLastUpdateTime
    }

    private fun cacheDirectory(): File = File(context.filesDir, CACHE_DIRECTORY_NAME)

    private fun metadataFile(): File = File(cacheDirectory(), CACHE_METADATA_FILE_NAME)

    private companion object {
        private const val CACHE_DIRECTORY_NAME = "menu-input-cache"
        private const val CACHE_METADATA_FILE_NAME = "remote-metadata.json"
    }
}

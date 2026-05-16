package com.example.mmapp.app2.data.input.source

import com.example.mmapp.app2.data.input.MenuSyncResult
import com.example.mmapp.app2.data.input.RawMenuLoadResult

class MenuInputLoader(
    private val bundledDataSource: BundledMenuInputDataSource,
    private val cacheStore: MenuInputCacheStore,
    private val expectedFileNames: Set<String>,
) {
    fun loadMenuJsonFiles(): RawMenuLoadResult {
        val cachedInputs = cacheStore.readInputs().filter { it.fileName in expectedFileNames }
        if (cachedInputs.isNotEmpty() && cacheStore.shouldUseCachedInputs()) {
            return RawMenuLoadResult(inputs = cachedInputs.sortedBy { it.fileName })
        }

        val warningMessage = if (cachedInputs.isNotEmpty()) {
            "Se usan los datos incluidos en esta versión de la aplicación."
        } else {
            "Se usan datos incluidos en la aplicación."
        }
        return RawMenuLoadResult(
            inputs = bundledDataSource.loadInputs(expectedFileNames),
            warningMessage = warningMessage,
        )
    }
}

class MenuInputSyncService(
    private val remoteDataSource: GithubMenuInputDataSource,
    private val cacheStore: MenuInputCacheStore,
) {
    fun syncFromGithub(): MenuSyncResult {
        val remoteFiles = remoteDataSource.loadRemoteFiles()
        require(remoteFiles.isNotEmpty()) { "No se pudo obtener el índice remoto." }

        val existingMetadata = cacheStore.readMetadata().associateBy { it.fileName }
        val cachedFileNames = cacheStore.readInputs().map { it.fileName }.toSet()
        var newCount = 0
        var updatedCount = 0
        var unchangedCount = 0

        remoteFiles.forEach { remoteFile ->
            val cached = existingMetadata[remoteFile.fileName]
            if (cached?.sha == remoteFile.sha && remoteFile.fileName in cachedFileNames) {
                unchangedCount++
            } else {
                val rawJson = remoteDataSource.downloadRawJson(remoteFile.downloadUrl)
                cacheStore.writeMenuFile(remoteFile.fileName, rawJson)
                if (cached == null) newCount++ else updatedCount++
            }
        }

        val removedCount = cacheStore.deleteMissingFiles(remoteFiles.map { it.fileName }.toSet())
        cacheStore.writeMetadata(remoteFiles.map { CachedMenuFile(it.fileName, it.sha) })

        return MenuSyncResult(
            newCount = newCount,
            updatedCount = updatedCount,
            removedCount = removedCount,
            unchangedCount = unchangedCount,
        )
    }
}

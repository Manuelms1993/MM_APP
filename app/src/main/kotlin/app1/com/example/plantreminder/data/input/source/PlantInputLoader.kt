package com.example.mmapp.app1.data.input.source

import com.example.mmapp.app1.data.input.PlantSyncResult
import com.example.mmapp.app1.data.input.RawPlantLoadResult

class PlantInputLoader(
    private val bundledDataSource: BundledPlantInputDataSource,
    private val cacheStore: PlantInputCacheStore,
) {
    fun loadPlantJsonFiles(): RawPlantLoadResult {
        val cachedInputs = cacheStore.readInputs()
        if (cachedInputs.isNotEmpty() && cacheStore.shouldUseCachedInputs()) {
            return RawPlantLoadResult(inputs = cachedInputs)
        }

        val warningMessage = if (cachedInputs.isNotEmpty()) {
            "Se usan los datos incluidos en esta versión de la aplicación."
        } else {
            "Se usan datos incluidos en la aplicación."
        }
        return RawPlantLoadResult(
            inputs = bundledDataSource.loadInputs(),
            warningMessage = warningMessage,
        )
    }
}

class PlantInputSyncService(
    private val remoteDataSource: GithubPlantInputDataSource,
    private val cacheStore: PlantInputCacheStore,
) {
    fun syncFromGithub(): PlantSyncResult {
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
                cacheStore.writePlantFile(remoteFile.fileName, rawJson)
                if (cached == null) newCount++ else updatedCount++
            }
        }

        val removedCount = cacheStore.deleteMissingFiles(remoteFiles.map { it.fileName }.toSet())
        cacheStore.writeMetadata(remoteFiles.map { CachedPlantFile(it.fileName, it.sha) })

        return PlantSyncResult(
            newCount = newCount,
            updatedCount = updatedCount,
            removedCount = removedCount,
            unchangedCount = unchangedCount,
        )
    }
}

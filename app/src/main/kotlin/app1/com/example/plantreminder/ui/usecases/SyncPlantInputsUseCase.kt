package com.example.mmapp.app1.ui.usecases

import com.example.mmapp.app1.data.repositories.PlantCatalogDataSource
import com.example.mmapp.app1.ui.HomeOperationMessageFormatter

class SyncPlantInputsUseCase(
    private val plantCatalogDataSource: PlantCatalogDataSource,
    private val messageFormatter: HomeOperationMessageFormatter,
) {
    suspend operator fun invoke(): String = runCatching {
        val result = plantCatalogDataSource.syncRemoteChanges()
        messageFormatter.format(result)
    }.getOrElse(messageFormatter::formatSyncError)
}

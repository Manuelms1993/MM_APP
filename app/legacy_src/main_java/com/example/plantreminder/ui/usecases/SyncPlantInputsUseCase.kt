package com.example.plantreminder.ui.usecases

import com.example.plantreminder.data.repositories.PlantCatalogDataSource
import com.example.plantreminder.ui.HomeOperationMessageFormatter

class SyncPlantInputsUseCase(
    private val plantCatalogDataSource: PlantCatalogDataSource,
    private val messageFormatter: HomeOperationMessageFormatter,
) {
    suspend operator fun invoke(): String {
        val result = plantCatalogDataSource.syncRemoteChanges()
        return messageFormatter.format(result)
    }
}

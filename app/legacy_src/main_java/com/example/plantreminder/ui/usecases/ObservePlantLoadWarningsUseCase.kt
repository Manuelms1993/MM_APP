package com.example.plantreminder.ui.usecases

import com.example.plantreminder.data.repositories.PlantCatalogDataSource
import kotlinx.coroutines.flow.StateFlow

class ObservePlantLoadWarningsUseCase(
    private val plantCatalogDataSource: PlantCatalogDataSource,
) {
    operator fun invoke(): StateFlow<String?> = plantCatalogDataSource.loadWarningMessage
}

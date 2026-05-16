package com.example.mmapp.app1.ui.usecases

import com.example.mmapp.app1.data.repositories.PlantCatalogDataSource
import kotlinx.coroutines.flow.StateFlow

class ObservePlantLoadWarningsUseCase(
    private val plantCatalogDataSource: PlantCatalogDataSource,
) {
    operator fun invoke(): StateFlow<String?> = plantCatalogDataSource.loadWarningMessage
}

package com.example.plantreminder.data.repositories

import com.example.plantreminder.data.input.PlantSyncResult
import com.example.plantreminder.domain.models.PlantDefinition
import kotlinx.coroutines.flow.StateFlow

interface PlantCatalogDataSource : PlantDefinitionDataSource {
    val loadWarningMessage: StateFlow<String?>

    suspend fun refreshLocalSource(): List<PlantDefinition>

    suspend fun syncRemoteChanges(): PlantSyncResult
}

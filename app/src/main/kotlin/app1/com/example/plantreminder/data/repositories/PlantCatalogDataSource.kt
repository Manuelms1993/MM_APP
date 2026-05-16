package com.example.mmapp.app1.data.repositories

import com.example.mmapp.app1.data.input.PlantSyncResult
import com.example.mmapp.app1.domain.models.PlantDefinition
import kotlinx.coroutines.flow.StateFlow

interface PlantCatalogDataSource : PlantDefinitionDataSource {
    val loadWarningMessage: StateFlow<String?>

    suspend fun refreshLocalSource(): List<PlantDefinition>

    suspend fun syncRemoteChanges(): PlantSyncResult
}

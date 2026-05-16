package com.example.plantreminder.data.repositories

import android.util.Log
import com.example.plantreminder.data.input.RawPlantLoadResult
import com.example.plantreminder.data.input.source.PlantInputSyncService
import com.example.plantreminder.data.input.source.PlantInputLoader
import com.example.plantreminder.data.input.validation.PlantCatalogValidator
import com.example.plantreminder.data.input.PlantSyncResult
import com.example.plantreminder.domain.models.PlantDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate

class PlantDefinitionRepository(
    private val plantInputLoader: PlantInputLoader,
    private val plantInputSyncService: PlantInputSyncService,
    private val plantCatalogValidator: PlantCatalogValidator,
) : PlantCatalogDataSource {
    private val cacheMutex = Mutex()

    @Volatile
    private var cachedPlants: List<PlantDefinition>? = null

    private val _loadWarningMessage = MutableStateFlow<String?>(null)
    override val loadWarningMessage: StateFlow<String?> = _loadWarningMessage.asStateFlow()

    override suspend fun getAllPlants(): List<PlantDefinition> = withContext(Dispatchers.IO) {
        cachedPlants ?: cacheMutex.withLock {
            cachedPlants ?: run {
                loadPlants(plantInputLoader.loadPlantJsonFiles())
            }
        }
    }

    override suspend fun getEarliestStartDate(): LocalDate? = getAllPlants().minOfOrNull { it.fechaInicio }

    override suspend fun refreshLocalSource(): List<PlantDefinition> = withContext(Dispatchers.IO) {
        cacheMutex.withLock {
            cachedPlants = null
        }
        getAllPlants()
    }

    override suspend fun syncRemoteChanges(): PlantSyncResult = withContext(Dispatchers.IO) {
        val result = plantInputSyncService.syncFromGithub()
        cacheMutex.withLock {
            cachedPlants = null
        }
        result
    }

    private fun loadPlants(loadResult: RawPlantLoadResult): List<PlantDefinition> {
        _loadWarningMessage.value = loadResult.warningMessage
        val validation = plantCatalogValidator.validate(loadResult.inputs)
        validation.warnings.forEach { warning -> Log.w(TAG, warning) }
        return validation.plants.also { cachedPlants = it }
    }

    companion object {
        private const val TAG = "PlantDefinitionRepo"
    }
}

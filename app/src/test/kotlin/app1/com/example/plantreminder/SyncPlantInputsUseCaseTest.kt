package com.example.mmapp.app1

import com.example.mmapp.app1.data.input.PlantSyncResult
import com.example.mmapp.app1.data.repositories.PlantCatalogDataSource
import com.example.mmapp.app1.domain.models.PlantDefinition
import com.example.mmapp.app1.ui.HomeOperationMessageFormatter
import com.example.mmapp.app1.ui.usecases.SyncPlantInputsUseCase
import com.example.mmapp.remote.RemoteSyncException
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SyncPlantInputsUseCaseTest {
    @Test
    fun `devuelve mensaje claro cuando no hay conexion`() = runTest {
        val dataSource = FakePlantCatalogDataSource(
            syncError = RemoteSyncException.NetworkUnavailable("sin red"),
        )

        val result = SyncPlantInputsUseCase(
            plantCatalogDataSource = dataSource,
            messageFormatter = HomeOperationMessageFormatter(),
        )()

        assertThat(result).isEqualTo("No se ha podido actualizar. No hay conexión con GitHub o con Internet.")
    }

    private class FakePlantCatalogDataSource(
        private val syncError: Throwable? = null,
    ) : PlantCatalogDataSource {
        override val loadWarningMessage = MutableStateFlow<String?>(null)

        override suspend fun getAllPlants(): List<PlantDefinition> = emptyList()

        override suspend fun getEarliestStartDate(): LocalDate? = null

        override suspend fun refreshLocalSource(): List<PlantDefinition> = emptyList()

        override suspend fun syncRemoteChanges(): PlantSyncResult {
            syncError?.let { throw it }
            return PlantSyncResult(0, 0, 0, 0)
        }
    }
}

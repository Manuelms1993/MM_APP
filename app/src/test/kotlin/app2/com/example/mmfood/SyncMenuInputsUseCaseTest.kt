package com.example.mmapp.app2

import com.example.mmapp.app2.data.input.MenuSyncResult
import com.example.mmapp.app2.data.repositories.MenuCatalogDataSource
import com.example.mmapp.app2.domain.models.MenuCatalog
import com.example.mmapp.app2.ui.HomeOperationMessageFormatter
import com.example.mmapp.app2.ui.usecases.SyncMenuInputsUseCase
import com.example.mmapp.remote.RemoteSyncException
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SyncMenuInputsUseCaseTest {
    @Test
    fun `devuelve mensaje claro cuando github responde con error`() = runTest {
        val dataSource = FakeMenuCatalogDataSource(
            syncError = RemoteSyncException.HttpError(503, "http 503"),
        )

        val result = SyncMenuInputsUseCase(
            menuCatalogDataSource = dataSource,
            messageFormatter = HomeOperationMessageFormatter(),
        )()

        assertThat(result).isEqualTo("No se ha podido actualizar. GitHub devolvió un error (503).")
    }

    private class FakeMenuCatalogDataSource(
        private val syncError: Throwable? = null,
    ) : MenuCatalogDataSource {
        override val loadWarningMessage = MutableStateFlow<String?>(null)

        override suspend fun getCatalog(): MenuCatalog {
            throw UnsupportedOperationException()
        }

        override suspend fun getEarliestStartDate(): LocalDate? = null

        override suspend fun refreshLocalSource(): MenuCatalog {
            throw UnsupportedOperationException()
        }

        override suspend fun syncRemoteChanges(): MenuSyncResult {
            syncError?.let { throw it }
            return MenuSyncResult(0, 0, 0, 0)
        }
    }
}
